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
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final String apiKey;
    private final String model;
    private final double temperature;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    
    public GeminiService(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.model}") String model,
            @Value("${gemini.api.temperature}") double temperature) {
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }
    
    public TriageResponse analyzeSymptoms(String voiceText) {
        long startTime = System.currentTimeMillis();
        
        try {
            String prompt = buildPrompt(voiceText);
            String response = callGeminiAPI(prompt);
            TriageResponse triageResponse = parseResponse(response);
            triageResponse.setResponseTimeMs(System.currentTimeMillis() - startTime);
            
            log.info("Triage completed in {} ms, severity: {}", 
                triageResponse.getResponseTimeMs(), triageResponse.getSeverity());
            
            return triageResponse;
            
        } catch (Exception e) {
            log.error("Error analyzing symptoms: {}", e.getMessage());
            return getFallbackResponse(System.currentTimeMillis() - startTime);
        }
    }
    
    private String buildPrompt(String voiceText) {
        return """
            You are a medical triage assistant. Analyze this voice transcript and return ONLY JSON.
            
            Transcript: "%s"
            
            Return JSON with this exact structure:
            {
              "symptoms": ["symptom1", "symptom2"],
              "severity": 5,
              "urgency": "immediate",
              "likely_condition": "condition name",
              "recommended_action": "call_ambulance",
              "first_aid_instructions": "clear instructions",
              "requires_emergency_contact": true
            }
            
            Rules:
            - severity: 1-10 (10=critical)
            - urgency: "immediate", "urgent", or "non-urgent"
            - recommended_action: "call_ambulance", "go_to_er", "see_doctor", or "home_care"
            - If life-threatening symptoms: severity >= 8
            - Always include clear first aid instructions
            """.formatted(voiceText);
    }
    
    private String callGeminiAPI(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        
        String requestBody = """
            {
                "contents": [{
                    "parts": [{"text": "%s"}]
                }],
                "generationConfig": {
                    "temperature": %f,
                    "topP": 0.95,
                    "topK": 40
                }
            }
            """.formatted(prompt.replace("\"", "\\\""), temperature);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API error: " + response.body());
        }
        
        // Parse response to extract text
        JsonNode root = objectMapper.readTree(response.body());
        String generatedText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        
        return generatedText;
    }
    
    private TriageResponse parseResponse(String jsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        List<String> symptoms = new ArrayList<>();
        if (root.has("symptoms") && root.get("symptoms").isArray()) {
            root.get("symptoms").forEach(node -> symptoms.add(node.asText()));
        }
        
        return TriageResponse.builder()
            .symptoms(symptoms.isEmpty() ? List.of("Unable to determine") : symptoms)
            .severity(root.has("severity") ? root.get("severity").asInt() : 0)
            .urgency(root.has("urgency") ? root.get("urgency").asText() : "non-urgent")
            .likelyCondition(root.has("likely_condition") ? root.get("likely_condition").asText() : "Unknown")
            .recommendedAction(root.has("recommended_action") ? root.get("recommended_action").asText() : "home_care")
            .firstAidInstructions(root.has("first_aid_instructions") ? root.get("first_aid_instructions").asText() : "Please rest and monitor symptoms")
            .requiresEmergencyContact(root.has("requires_emergency_contact") && root.get("requires_emergency_contact").asBoolean())
            .build();
    }
    
    private TriageResponse getFallbackResponse(long responseTime) {
        return TriageResponse.builder()
            .symptoms(List.of("Unable to analyze"))
            .severity(0)
            .urgency("non-urgent")
            .likelyCondition("Unable to determine")
            .recommendedAction("home_care")
            .firstAidInstructions("Please rest and consult a doctor if symptoms persist")
            .requiresEmergencyContact(false)
            .responseTimeMs(responseTime)
            .build();
    }
}