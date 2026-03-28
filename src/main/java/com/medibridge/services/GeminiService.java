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
        this.model = normalizeModelId(model);
        this.temperature = temperature;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }
    
    public TriageResponse analyzeSymptoms(String voiceText) {
        return analyzeSymptoms(voiceText, null);
    }

    public TriageResponse analyzeSymptoms(String voiceText, String medicalHistory) {
        long startTime = System.currentTimeMillis();

        if (apiKey == null || apiKey.isBlank()) {
            log.error("GEMINI_API_KEY is missing or empty.");
            return getFallbackResponse(System.currentTimeMillis() - startTime);
        }

        try {
            String prompt = buildPrompt(voiceText, medicalHistory);
            String response = callGeminiAPI(prompt);
            
            // Clean markdown json formatting if Gemini includes it
            if (response.startsWith("```json")) {
                response = response.substring(7);
            }
            if (response.startsWith("```")) {
                response = response.substring(3);
            }
            if (response.endsWith("```")) {
                response = response.substring(0, response.length() - 3);
            }
            
            TriageResponse triageResponse = parseResponse(response.trim());
            triageResponse.setResponseTimeMs(System.currentTimeMillis() - startTime);
            
            log.info("Triage completed in {} ms, severity: {}", 
                triageResponse.getResponseTimeMs(), triageResponse.getSeverity());
            
            return triageResponse;
            
        } catch (Exception e) {
            log.error("Error analyzing symptoms: {}", e.getMessage());
            return getFallbackResponse(System.currentTimeMillis() - startTime);
        }
    }
    
    private String buildPrompt(String voiceText, String medicalHistory) {
        String historySection = (medicalHistory != null && !medicalHistory.isBlank()) 
            ? "\n\nMessy Medical History (MUST FACTOR INTO ANALYSIS):\n" + medicalHistory 
            : "";
            
        return """
            SYSTEM: You are a critical care medical triage system (EMD).
            Analyze the following emergency input and messy medical history.
            
            USER SYMPTOMS: "%s"%s
            
            TASK: Return a JSON object with this EXACT structure (NO OTHER TEXT):
            {
              "symptoms": ["list", "of", "symptoms"],
              "severity": 1-10,
              "urgency": "immediate" | "urgent" | "non-urgent",
              "likely_condition": "Short medical name",
              "recommended_action": "call_ambulance" | "go_to_er" | "see_doctor" | "home_care",
              "first_aid_instructions": "Step-by-step clear life saving instructions",
              "requires_emergency_contact": boolean
            }
            
            CRITICAL RULES:
            1. If symptoms involve: CANNOT BREATHE, CHEST PAIN, STROKE, or UNCONSCIOUS -> Severity MUST be 10 and action MUST be "call_ambulance".
            2. Factor the "Messy Medical History" into the first_aid_instructions (e.g., if allergic to something, warn against it).
            3. Return only the raw JSON string. Do not use Markdown.
            """.formatted(voiceText, historySection);
    }
    
    /**
     * REST path expects only the model id (e.g. {@code gemini-2.0-flash}), not {@code models/...}.
     */
    private static String normalizeModelId(String configured) {
        if (configured == null || configured.isBlank()) {
            return "gemini-2.0-flash";
        }
        String id = configured.trim();
        if (id.startsWith("models/")) {
            id = id.substring("models/".length());
        }
        return id;
    }

    private String callGeminiAPI(String prompt) throws Exception {
        // Switch to query parameter to ensure compatibility across all API keys and regions
        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey.trim();

        String requestBody = """
            {
                "contents": [{
                    "parts": [{"text": "%s"}]
                }],
                "generationConfig": {
                    "temperature": %f,
                    "topP": 0.95,
                    "topK": 40,
                    "responseMimeType": "application/json"
                }
            }
            """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"), temperature);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            log.error("Gemini API Error {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Gemini API error: " + response.body());
        }
        
        JsonNode root = objectMapper.readTree(response.body());
        return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
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