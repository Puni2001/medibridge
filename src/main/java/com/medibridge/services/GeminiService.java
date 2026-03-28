package com.medibridge.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibridge.dto.TriageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiService {
    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final String apiKey;
    private final String model;
    private final double temperature;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiService(@Value("${gemini.api.key:}") String apiKey,
                         @Value("${gemini.model:gemini-1.5-flash}") String model,
                         @Value("${gemini.temperature:0.7}") double temperature) {
        this.apiKey = apiKey;
        this.model = normalizeModelId(model);
        this.temperature = temperature;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public TriageResponse analyzeSymptoms(String voiceText, String medicalHistory) {
        long startTime = System.currentTimeMillis();
        if (apiKey == null || apiKey.isBlank()) {
            return getFallbackResponse(voiceText, System.currentTimeMillis() - startTime);
        }

        try {
            log.info("[AI ANALYSIS START] Model: {}, Symptoms Length: {} characters", model, voiceText != null ? voiceText.length() : 0);
            String prompt = buildPrompt(voiceText, medicalHistory);
            String response = callGeminiAPI(prompt);
            
            TriageResponse triageResponse = parseResponse(response.trim());
            long latency = System.currentTimeMillis() - startTime;
            log.info("[AI ANALYSIS SUCCESS] Latency: {}ms, Severity: {}/10, Urgency: {}", latency, triageResponse.getSeverity(), triageResponse.getUrgency());
            
            triageResponse.setResponseTimeMs(latency);
            return triageResponse;
        } catch (Exception e) {
            log.error("[AI ANALYSIS FAILED] Error: {}", e.getMessage());
            return getFallbackResponse(voiceText, System.currentTimeMillis() - startTime);
        }
    }

    private String buildPrompt(String voiceText, String medicalHistory) {
        return "Task: emergency triage for input: " + voiceText + " with history: " + medicalHistory + 
               ". Return raw JSON: {symptoms[], severity[1-10], urgency, likely_condition, recommended_action, first_aid_instructions, requires_emergency_contact[bool]}";
    }

    private String callGeminiAPI(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        String body = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt + "\"}]}]}";
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(res.body());
        return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
    }

    private TriageResponse parseResponse(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        List<String> symptoms = new ArrayList<>();
        if (root.path("symptoms").isArray()) root.path("symptoms").forEach(n -> symptoms.add(n.asText()));

        return new TriageResponse(
            symptoms,
            root.path("severity").asInt(0),
            root.path("urgency").asText("non-urgent"),
            root.path("likely_condition").asText("Unknown"),
            root.path("recommended_action").asText("home_care"),
            root.path("first_aid_instructions").asText("Monitor symptoms"),
            root.path("requires_emergency_contact").asBoolean(false),
            0L
        );
    }

    private TriageResponse getFallbackResponse(String input, long time) {
        int sev = 0;
        String action = "home_care";
        String inst = "Seek help if symptoms persist.";
        String lowerInput = (input != null) ? input.toLowerCase() : "";
        
        // Comprehensive Emergency Keyword Engine (Fail-Safe)
        if (lowerInput.contains("breath") || lowerInput.contains("chest") || lowerInput.contains("heart") || 
            lowerInput.contains("pain") || lowerInput.contains("bleed") || lowerInput.contains("stroke") ||
            lowerInput.contains("see") || lowerInput.contains("vision") || lowerInput.contains("blind") ||
            lowerInput.contains("eye") || lowerInput.contains("faint") || lowerInput.contains("conscious")) {
            
            sev = 10; 
            action = "call_ambulance"; 
            inst = "🚨 CRITICAL: CALL EMERGENCY SERVICES IMMEDIATELY! Possible stroke or severe injury detected. Keep the person still.";
        }
        
        return new TriageResponse(List.of("Fallback Analysis (Safety Engine Active)"), sev, sev >= 8 ? "immediate" : "non-urgent", 
                                  "Emergency Rule-Engine Match", action, inst, sev >= 8, time);
    }

    private static String normalizeModelId(String m) { return m != null && m.startsWith("models/") ? m.substring(7) : (m == null ? "gemini-1.5-flash" : m); }
}