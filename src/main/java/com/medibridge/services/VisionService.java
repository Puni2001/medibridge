package com.medibridge.services;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.medibridge.dto.WoundAnalysis;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VisionService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VisionService.class);
    
    public WoundAnalysis analyzeWound(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) return null;
        try {
            try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
                ByteString imgBytes = ByteString.copyFrom(imageFile.getBytes());
                Image img = Image.newBuilder().setContent(imgBytes).build();
                
                List<AnnotateImageRequest> requests = new ArrayList<>();
                Feature featLabel = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
                
                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(featLabel)
                    .setImage(img)
                    .build();
                requests.add(request);
                
                BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();
                
                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        log.error("Vision API error: {}", res.getError().getMessage());
                        return getFallbackAnalysis();
                    }
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
        StringBuilder desc = new StringBuilder();
        String severity = "moderate";
        for (EntityAnnotation label : labels) {
            desc.append(label.getDescription()).append(", ");
            if (label.getDescription().toLowerCase().contains("urgent") || label.getDescription().toLowerCase().contains("deep")) {
                severity = "critical";
            }
        }
        return new WoundAnalysis(desc.toString(), severity, "Clean with sterile water and apply pressure.");
    }
    
    private WoundAnalysis getFallbackAnalysis() {
        return new WoundAnalysis("Unable to analyze photo", "unknown", "Please consult a healthcare professional.");
    }
}
