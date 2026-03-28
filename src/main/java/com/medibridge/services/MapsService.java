package com.medibridge.services;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlaceType;
import com.google.maps.model.LatLng;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MapsService {

    private final String apiKey;
    private final GeoApiContext context;

    public MapsService(@Value("${google.maps.api.key:}") String apiKey) {
        this.apiKey = apiKey;
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public List<String> findNearestHospitals(double lat, double lng) {
        List<String> hospitals = new ArrayList<>();
        
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Google Maps API key is missing. Skipping hospital search.");
            return List.of("ER at City General (Demo)", "St. Jude Medical Center (Demo)");
        }

        try {
            LatLng location = new LatLng(lat, lng);
            PlacesSearchResponse response = PlacesApi.nearbySearchQuery(context, location)
                    .radius(5000)
                    .type(PlaceType.HOSPITAL)
                    .await();

            if (response.results != null) {
                for (int i = 0; i < Math.min(3, response.results.length); i++) {
                    hospitals.add(response.results[i].name + " - " + response.results[i].vicinity);
                }
            }
        } catch (Exception e) {
            log.error("Failed to find nearest hospitals: {}", e.getMessage());
            hospitals.add("Unable to locate nearby hospitals real-time.");
        }
        
        if (hospitals.isEmpty()) {
            hospitals.add("No hospitals found within 5km of your location.");
        }
        
        return hospitals;
    }
}
