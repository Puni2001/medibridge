package com.medibridge.services;

import com.medibridge.dto.TriageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class GeminiServiceTest {

    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        geminiService = new GeminiService("test-key", "gemini-pro", 0.7);
    }

    @Test
    void testNormalizeModelId() {
        String result = (String) ReflectionTestUtils.invokeMethod(geminiService, "normalizeModelId", "models/gemini-1.5-flash");
        assertEquals("gemini-1.5-flash", result);
    }

    @Test
    void testFallbackResponse() {
        TriageResponse response = ReflectionTestUtils.invokeMethod(geminiService, "getFallbackResponse", 100L);
        assertNotNull(response);
        assertEquals(0, response.getSeverity());
        assertEquals("Unable to analyze", response.getSymptoms().get(0));
    }

    @Test
    void testPromptBuilding() {
        String prompt = ReflectionTestUtils.invokeMethod(geminiService, "buildPrompt", "chest pain", "diabetic");
        assertTrue(prompt.contains("chest pain"));
        assertTrue(prompt.contains("diabetic"));
        assertTrue(prompt.contains("Severity MUST be 10"));
    }
}
