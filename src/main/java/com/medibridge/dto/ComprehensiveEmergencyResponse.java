package com.medibridge.dto;

import java.util.List;

public class ComprehensiveEmergencyResponse {
    private TriageResponse triage;
    private WoundAnalysis wound;
    private List<HospitalInfo> hospitals;
    private String ttsAudioBase64;

    public ComprehensiveEmergencyResponse() {}

    public ComprehensiveEmergencyResponse(TriageResponse triage, WoundAnalysis wound, List<HospitalInfo> hospitals, String ttsAudioBase64) {
        this.triage = triage;
        this.wound = wound;
        this.hospitals = hospitals;
        this.ttsAudioBase64 = ttsAudioBase64;
    }

    public static builder Builder() {
        return new builder();
    }

    public static class builder {
        private TriageResponse triage;
        private WoundAnalysis wound;
        private List<HospitalInfo> hospitals;
        private String ttsAudioBase64;

        public builder triage(TriageResponse t) { this.triage = t; return this; }
        public builder wound(WoundAnalysis w) { this.wound = w; return this; }
        public builder hospitals(List<HospitalInfo> h) { this.hospitals = h; return this; }
        public builder ttsAudioBase64(String t) { this.ttsAudioBase64 = t; return this; }
        public ComprehensiveEmergencyResponse build() {
            return new ComprehensiveEmergencyResponse(triage, wound, hospitals, ttsAudioBase64);
        }
    }

    public TriageResponse getTriage() { return triage; }
    public WoundAnalysis getWound() { return wound; }
    public List<HospitalInfo> getHospitals() { return hospitals; }
    public String getTtsAudioBase64() { return ttsAudioBase64; }

    public void setTriage(TriageResponse triage) { this.triage = triage; }
    public void setWound(WoundAnalysis wound) { this.wound = wound; }
    public void setHospitals(List<HospitalInfo> hospitals) { this.hospitals = hospitals; }
    public void setTtsAudioBase64(String ttsAudioBase64) { this.ttsAudioBase64 = ttsAudioBase64; }

    public static class HospitalInfo {
        private String name;
        private String address;
        private double distance;

        public HospitalInfo() {}

        public HospitalInfo(String name, String address, double distance) {
            this.name = name;
            this.address = address;
            this.distance = distance;
        }

        public String getName() { return name; }
        public String getAddress() { return address; }
        public double getDistance() { return distance; }
    }
}
