package com.medibridge.services;

import org.springframework.stereotype.Service;

@Service
public class TTSService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TTSService.class);
    
    public String synthesize(String text) {
        log.info("Synthesizing speech...");
        // Placeholder: would return base64 audio in full implementation
        return ""; 
    }
}
