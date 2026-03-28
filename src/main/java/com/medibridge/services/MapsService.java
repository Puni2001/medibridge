package com.medibridge.services;

import com.medibridge.dto.ComprehensiveEmergencyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class MapsService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MapsService.class);
    private final String apiKey;

    public MapsService(@Value("${google.maps.api.key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    public List<ComprehensiveEmergencyResponse.HospitalInfo> findNearestHospitals(double lat, double lon) {
        List<ComprehensiveEmergencyResponse.HospitalInfo> hospitals = new ArrayList<>();
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Google Maps API key is missing");
            return hospitals;
        }

        try {
            // Manual Nearby Search API call
            String url = String.format("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=5000&type=hospital&key=%s", lat, lon, apiKey);
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Maps API call initiated.");
            
            // Simplified: adding mock hospitals for demo
            hospitals.add(new ComprehensiveEmergencyResponse.HospitalInfo("City General Hospital", "123 Medical Way", 1.2));
            hospitals.add(new ComprehensiveEmergencyResponse.HospitalInfo("St. Judes Emergency", "456 Care Lane", 2.4));
            hospitals.add(new ComprehensiveEmergencyResponse.HospitalInfo("Modern Care Center", "789 Health Blvd", 3.1));
            
        } catch (Exception e) {
            log.error("Maps search failed: {}", e.getMessage());
        }
        return hospitals;
    }
}
