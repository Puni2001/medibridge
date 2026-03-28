package com.medibridge.controller;

import com.medibridge.dto.ComprehensiveEmergencyResponse;
import com.medibridge.dto.TriageResponse;
import com.medibridge.dto.WoundAnalysis;
import com.medibridge.services.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AdvancedTriageController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdvancedTriageController.class);

    private final GeminiService geminiService;
    private final VisionService visionService;
    private final TTSService ttsService;
    private final TranslationService translationService;
    private final MapsService mapsService;
    private final RateLimitService rateLimitService;

    public AdvancedTriageController(GeminiService geminiService, VisionService visionService,
                                   TTSService ttsService, TranslationService translationService,
                                   MapsService mapsService, RateLimitService rateLimitService) {
        this.geminiService = geminiService;
        this.visionService = visionService;
        this.ttsService = ttsService;
        this.translationService = translationService;
        this.mapsService = mapsService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping(value = "/emergency", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComprehensiveEmergencyResponse> emergency(
            @RequestParam(value = "voiceText", required = false) String voiceText,
            @RequestParam(value = "medicalHistory", required = false) String history,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "lang", defaultValue = "en") String lang,
            @RequestParam(value = "lat", defaultValue = "0") double lat,
            @RequestParam(value = "lon", defaultValue = "0") double lon) {

        long startTime = System.currentTimeMillis();
        log.info("Emergency Request: voiceText={}, lang={}", voiceText, lang);

        TriageResponse triage = geminiService.analyzeSymptoms(voiceText, history);
        WoundAnalysis wound = visionService.analyzeWound(image);
        String translated = translationService.translate(triage.getRecommendedAction(), lang);
        String tts = ttsService.synthesize(translated);
        List<ComprehensiveEmergencyResponse.HospitalInfo> hospitals = mapsService.findNearestHospitals(lat, lon);

        ComprehensiveEmergencyResponse response = new ComprehensiveEmergencyResponse(
            triage, wound, hospitals, tts
        );

        return ResponseEntity.ok(response);
    }
}
