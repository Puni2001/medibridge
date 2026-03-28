package com.medibridge.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ComprehensiveEmergencyResponse {
    private TriageResponse triage;
    private WoundAnalysis woundAnalysis;
    private String firstAidDiagram; // URL from Imagen
    private byte[] voiceInstructions;
    private TranslatedEmergency translatedInstructions;
    private Long responseTimeMs;
}
