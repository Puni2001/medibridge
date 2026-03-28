package com.medibridge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranslatedEmergency {
    private String originalText;
    private String translatedText;
    private String medicalTerms;
    private String sourceLanguage;
    private String targetLanguage;
}
