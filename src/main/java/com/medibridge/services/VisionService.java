package com.medibridge.services;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.medibridge.dto.WoundAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class VisionService {
    
    public WoundAnalysis analyzeWound(MultipartFile imageFile) {
        try {
            // Initialize Vision client
            try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
                
                // Convert image to byte array
                ByteString imgBytes = ByteString.copyFrom(imageFile.getBytes());
                Image img = Image.newBuilder().setContent(imgBytes).build();
                
                // Detect labels (what's in the image)
                List<AnnotateImageRequest> requests = new ArrayList<>();
                Feature featLabel = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
                Feature featSafeSearch = Feature.newBuilder().setType(Feature.Type.SAFE_SEARCH_DETECTION).build();
                
                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(featLabel)
                    .addFeatures(featSafeSearch)
                    .setImage(img)
                    .build();
                requests.add(request);
                
                // Perform request
                BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();
                
                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        log.error("Vision API error: {}", res.getError().getMessage());
                        return getFallbackAnalysis();
                    }
                    
                    // Analyze labels to determine wound type
                    List<EntityAnnotation> labels = res.getLabelAnnotationsList();
                    return analyzeLabels(labels);
                }
            }
        } catch (IOException e) {
            log.error("Vision analysis failed: {}", e.getMessage());
        }
        return getFallbackAnalysis();
    }
    
    private WoundAnalysis analyzeLabels(List<EntityAnnotation> labels) {
        WoundAnalysis.WoundAnalysisBuilder analysis = WoundAnalysis.builder();
        
        boolean hasWound = false;
        boolean hasBleeding = false;
        float confidence = 0;
        
        for (EntityAnnotation label : labels) {
            String description = label.getDescription().toLowerCase();
            float score = label.getScore();
            
            if (description.contains("wound") || description.contains("cut") || 
                description.contains("injury") || description.contains("laceration")) {
                hasWound = true;
                confidence = score;
                analysis.woundType(determineWoundType(description));
            }
            if (description.contains("blood") || description.contains("bleeding")) {
                hasBleeding = true;
            }
        }
        
        if (hasWound) {
            analysis.severity(hasBleeding ? "moderate" : "mild")
                .needsStitches(confidence > 0.8)
                .bleeding(hasBleeding ? "active" : "none")
                .infectionRisk(confidence > 0.7 ? "moderate" : "low")
                .recommendations(generateRecommendations(hasBleeding));
        }
        
        return analysis.build();
    }
    
    private String determineWoundType(String description) {
        if (description.contains("cut")) return "laceration";
        if (description.contains("burn")) return "burn";
        if (description.contains("bruise")) return "contusion";
        return "abrasion";
    }
    
    private List<String> generateRecommendations(boolean hasBleeding) {
        List<String> recs = new ArrayList<>();
        recs.add("Clean wound with sterile water");
        if (hasBleeding) {
            recs.add("Apply direct pressure with clean cloth");
            recs.add("Elevate wound above heart if possible");
        }
        recs.add("Cover with sterile bandage");
        recs.add("Seek medical attention if bleeding doesn't stop");
        return recs;
    }
    
    private WoundAnalysis getFallbackAnalysis() {
        return WoundAnalysis.builder()
            .woundType("unable to determine")
            .severity("unknown")
            .needsStitches(false)
            .bleeding("unknown")
            .infectionRisk("unknown")
            .recommendations(List.of("Please clean wound and seek medical attention"))
            .build();
    }
}
