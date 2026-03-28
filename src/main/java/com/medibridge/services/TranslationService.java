package com.medibridge.services;

import org.springframework.stereotype.Service;

@Service
public class TranslationService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TranslationService.class);
    
    public String translate(String text, String targetLang) {
        log.info("Translating to: {}", targetLang);
        // Placeholder: in a full impl this would call Google Translate
        return text; 
    }
}
