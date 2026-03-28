package com.medibridge.controller;

import com.medibridge.dto.ComprehensiveEmergencyResponse;
import com.medibridge.dto.TranslatedEmergency;
import com.medibridge.dto.TriageResponse;
import com.medibridge.dto.WoundAnalysis;
import com.medibridge.services.GeminiService;
import com.medibridge.services.TTSService;
import com.medibridge.services.TranslationService;
import com.medibridge.services.VisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v2")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AdvancedTriageController {

    private final GeminiService geminiService;
    private final VisionService visionService;
    private final TTSService ttsService;
    private final TranslationService translationService;

    @PostMapping(value = "/emergency", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComprehensiveEmergencyResponse> emergency(
            @RequestParam(value = "voiceText", required = false) String voiceText,
            @RequestParam(value = "medicalHistory", required = false) String medicalHistory,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "language", defaultValue = "en") String language) {

        long startTime = System.currentTimeMillis();
        
        TriageResponse triage = null;
        WoundAnalysis woundAnalysis = null;
        TranslatedEmergency translation = null;
        byte[] voiceInstructions = null;

        if (voiceText != null && !voiceText.isBlank()) {
            triage = geminiService.analyzeSymptoms(voiceText, medicalHistory);
            
            if (!language.equals("en") && triage != null && triage.getFirstAidInstructions() != null) {
                translation = translationService.translateEmergency(
                        triage.getFirstAidInstructions(), language);
            }
            
            String textToSpeak = translation != null ? translation.getTranslatedText() : 
                                (triage != null ? triage.getFirstAidInstructions() : "Please seek medical attention.");
            voiceInstructions = ttsService.generateVoiceInstructions(textToSpeak, language);
        }

        if (image != null && !image.isEmpty()) {
            woundAnalysis = visionService.analyzeWound(image);
        }

        ComprehensiveEmergencyResponse response = ComprehensiveEmergencyResponse.builder()
                .triage(triage)
                .woundAnalysis(woundAnalysis)
                .translatedInstructions(translation)
                .voiceInstructions(voiceInstructions)
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .build();

        return ResponseEntity.ok(response);
    }
}
