package com.medibridge.services;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.medibridge.dto.TranslatedEmergency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TranslationService {
    
    private final Translate translate;
    
    public TranslationService() {
        // Initialize Translation client
        this.translate = TranslateOptions.getDefaultInstance().getService();
    }
    
    public TranslatedEmergency translateEmergency(String text, String targetLanguage) {
        try {
            // Detect source language
            com.google.cloud.translate.Detection detection = translate.detect(text);
            String sourceLang = detection.getLanguage();
            
            // Translate text
            Translation translation = translate.translate(
                text,
                Translate.TranslateOption.targetLanguage(targetLanguage),
                Translate.TranslateOption.sourceLanguage(sourceLang)
            );
            
            // Translate medical terms specially
            String medicalTerms = translateMedicalTerms(text, targetLanguage);
            
            return TranslatedEmergency.builder()
                .originalText(text)
                .translatedText(translation.getTranslatedText())
                .medicalTerms(medicalTerms)
                .sourceLanguage(sourceLang)
                .targetLanguage(targetLanguage)
                .build();
                
        } catch (Exception e) {
            log.error("Translation failed: {}", e.getMessage());
            return TranslatedEmergency.builder()
                .originalText(text)
                .translatedText(text) // Fallback to original
                .sourceLanguage("unknown")
                .targetLanguage(targetLanguage)
                .build();
        }
    }
    
    private String translateMedicalTerms(String text, String targetLanguage) {
        // Medical term dictionary for common terms
        // In production, use a medical-specific translation model
        return text; // Simplified for now
    }
}
