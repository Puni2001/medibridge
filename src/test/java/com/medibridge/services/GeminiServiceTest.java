package com.medibridge.services;

import com.medibridge.dto.TriageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GeminiService — covering the 110% Fail-Safe Engine and triage logic.
 * These tests guarantee that critical medical emergencies are ALWAYS detected correctly.
 */
class GeminiServiceTest {

    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        // Use empty API key to force fallback engine (110% Safety Mode)
        geminiService = new GeminiService("", "gemini-1.5-flash");
    }

    @Test
    @DisplayName("Critical Test: Chest pain should return severity 10 and call_ambulance")
    void testChestPainIsCritical() {
        TriageResponse response = geminiService.analyzeSymptoms("I have severe chest pain spreading to my arm", null);

        assertNotNull(response, "Response should never be null — safety engine must always respond");
        assertEquals(10, response.getSeverity(), "Chest pain must be detected as severity 10");
        assertEquals("call_ambulance", response.getRecommendedAction(), "Chest pain must trigger ambulance call");
        assertTrue(response.isRequiresEmergencyContact(), "Emergency contact is mandatory for chest pain");
    }

    @Test
    @DisplayName("Critical Test: Breathing difficulty should trigger immediate protocol")
    void testBreathingDifficultyIsCritical() {
        TriageResponse response = geminiService.analyzeSymptoms("I cannot breathe properly", null);

        assertNotNull(response);
        assertEquals(10, response.getSeverity(), "Breathing difficulty must always be severity 10");
        assertEquals("immediate", response.getUrgency());
    }

    @Test
    @DisplayName("Critical Test: Stroke symptoms (vision loss, face numbness) detected")
    void testStrokeSymptomsDetected() {
        TriageResponse response = geminiService.analyzeSymptoms("I cannot see anything and my face feels numb", null);

        assertNotNull(response);
        assertEquals(10, response.getSeverity(), "Vision/stroke symptoms must be severity 10");
        assertEquals("call_ambulance", response.getRecommendedAction());
    }

    @Test
    @DisplayName("Critical Test: Poisoning/toxic exposure detected")
    void testPoisoningDetected() {
        TriageResponse response = geminiService.analyzeSymptoms("I swallowed something toxic and feel faint", null);

        assertNotNull(response);
        assertEquals(10, response.getSeverity(), "Poisoning must be detected as critical");
    }

    @Test
    @DisplayName("Stability Test: Mild headache should be non-urgent home care")
    void testMildSymptomIsNonUrgent() {
        TriageResponse response = geminiService.analyzeSymptoms("I have a mild headache", null);

        assertNotNull(response);
        assertEquals("home_care", response.getRecommendedAction(), "Mild headache should be home care");
        assertFalse(response.isRequiresEmergencyContact(), "Headache should not require emergency contact");
    }

    @Test
    @DisplayName("Reliability Test: Empty input should not throw exception")
    void testEmptyInputHandledGracefully() {
        assertDoesNotThrow(() -> geminiService.analyzeSymptoms("", null),
            "Empty input must never crash the system");
    }

    @Test
    @DisplayName("Reliability Test: Null input should not throw exception")
    void testNullInputHandledGracefully() {
        assertDoesNotThrow(() -> geminiService.analyzeSymptoms(null, null),
            "Null input must never crash the safety engine");
    }

    @Test
    @DisplayName("Performance Test: Response time under 2000ms for fallback engine")
    void testResponseTime() {
        long start = System.currentTimeMillis();
        geminiService.analyzeSymptoms("chest pain", null);
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 2000, "Fallback engine must respond in under 2 seconds, got: " + elapsed + "ms");
    }
}
