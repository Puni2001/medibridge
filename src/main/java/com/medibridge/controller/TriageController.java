package com.medibridge.controller;

import com.medibridge.dto.TriageRequest;
import com.medibridge.dto.TriageResponse;
import com.medibridge.services.GeminiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TriageController {

    private static final Logger log = LoggerFactory.getLogger(TriageController.class);

    private final GeminiService geminiService;

    public TriageController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/triage")
    public ResponseEntity<TriageResponse> triage(@Valid @RequestBody TriageRequest request) {
        String text = request.getVoiceText();
        log.info(
                "Received triage request with text: {}",
                text.substring(0, Math.min(50, text.length())));

        TriageResponse response = geminiService.analyzeSymptoms(text);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MediBridge is running!");
    }
}
