package com.medibridge.dto;

import java.util.List;

public class TriageResponse {
    private List<String> symptoms;
    private int severity;
    private String urgency;
    private String likelyCondition;
    private String recommendedAction;
    private String firstAidInstructions;
    private boolean requiresEmergencyContact;
    private Long responseTimeMs;

    public TriageResponse() {}

    public TriageResponse(List<String> symptoms, int severity, String urgency, String likelyCondition, 
                           String recommendedAction, String firstAidInstructions, boolean requiresEmergencyContact, 
                           Long responseTimeMs) {
        this.symptoms = symptoms;
        this.severity = severity;
        this.urgency = urgency;
        this.likelyCondition = likelyCondition;
        this.recommendedAction = recommendedAction;
        this.firstAidInstructions = firstAidInstructions;
        this.requiresEmergencyContact = requiresEmergencyContact;
        this.responseTimeMs = responseTimeMs;
    }

    public static TriageResponseBuilder builder() {
        return new TriageResponseBuilder();
    }

    public static class TriageResponseBuilder {
        private List<String> symptoms;
        private int severity;
        private String urgency;
        private String likelyCondition;
        private String recommendedAction;
        private String firstAidInstructions;
        private boolean requiresEmergencyContact;
        private Long responseTimeMs;

        public TriageResponseBuilder symptoms(List<String> symptoms) { this.symptoms = symptoms; return this; }
        public TriageResponseBuilder severity(int severity) { this.severity = severity; return this; }
        public TriageResponseBuilder urgency(String urgency) { this.urgency = urgency; return this; }
        public TriageResponseBuilder likelyCondition(String condition) { this.likelyCondition = condition; return this; }
        public TriageResponseBuilder recommendedAction(String action) { this.recommendedAction = action; return this; }
        public TriageResponseBuilder firstAidInstructions(String instr) { this.firstAidInstructions = instr; return this; }
        public TriageResponseBuilder requiresEmergencyContact(boolean req) { this.requiresEmergencyContact = req; return this; }
        public TriageResponseBuilder responseTimeMs(Long time) { this.responseTimeMs = time; return this; }
        public TriageResponse build() {
            return new TriageResponse(symptoms, severity, urgency, likelyCondition, recommendedAction, firstAidInstructions, requiresEmergencyContact, responseTimeMs);
        }
    }

    public List<String> getSymptoms() { return symptoms; }
    public int getSeverity() { return severity; }
    public String getUrgency() { return urgency; }
    public String getLikelyCondition() { return likelyCondition; }
    public String getRecommendedAction() { return recommendedAction; }
    public String getFirstAidInstructions() { return firstAidInstructions; }
    public boolean isRequiresEmergencyContact() { return requiresEmergencyContact; }
    public Long getResponseTimeMs() { return responseTimeMs; }

    public void setResponseTimeMs(Long time) { this.responseTimeMs = time; }
    public void setSymptoms(List<String> s) { this.symptoms = s; }
    public void setSeverity(int s) { this.severity = s; }
    public void setUrgency(String u) { this.urgency = u; }
    public void setLikelyCondition(String l) { this.likelyCondition = l; }
    public void setRecommendedAction(String r) { this.recommendedAction = r; }
    public void setFirstAidInstructions(String f) { this.firstAidInstructions = f; }
    public void setRequiresEmergencyContact(boolean r) { this.requiresEmergencyContact = r; }
}
