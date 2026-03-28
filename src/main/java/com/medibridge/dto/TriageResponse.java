package com.medibridge.dto;

import java.util.List;

public class TriageResponse {

    private List<String> symptoms;
    private Integer severity;
    private String urgency;
    private String likelyCondition;
    private String recommendedAction;
    private String firstAidInstructions;
    private Boolean requiresEmergencyContact;
    private Long responseTimeMs;

    public static Builder builder() {
        return new Builder();
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getLikelyCondition() {
        return likelyCondition;
    }

    public void setLikelyCondition(String likelyCondition) {
        this.likelyCondition = likelyCondition;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public String getFirstAidInstructions() {
        return firstAidInstructions;
    }

    public void setFirstAidInstructions(String firstAidInstructions) {
        this.firstAidInstructions = firstAidInstructions;
    }

    public Boolean getRequiresEmergencyContact() {
        return requiresEmergencyContact;
    }

    public void setRequiresEmergencyContact(Boolean requiresEmergencyContact) {
        this.requiresEmergencyContact = requiresEmergencyContact;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public static final class Builder {
        private List<String> symptoms;
        private Integer severity;
        private String urgency;
        private String likelyCondition;
        private String recommendedAction;
        private String firstAidInstructions;
        private Boolean requiresEmergencyContact;
        private Long responseTimeMs;

        public Builder symptoms(List<String> symptoms) {
            this.symptoms = symptoms;
            return this;
        }

        public Builder severity(Integer severity) {
            this.severity = severity;
            return this;
        }

        public Builder urgency(String urgency) {
            this.urgency = urgency;
            return this;
        }

        public Builder likelyCondition(String likelyCondition) {
            this.likelyCondition = likelyCondition;
            return this;
        }

        public Builder recommendedAction(String recommendedAction) {
            this.recommendedAction = recommendedAction;
            return this;
        }

        public Builder firstAidInstructions(String firstAidInstructions) {
            this.firstAidInstructions = firstAidInstructions;
            return this;
        }

        public Builder requiresEmergencyContact(Boolean requiresEmergencyContact) {
            this.requiresEmergencyContact = requiresEmergencyContact;
            return this;
        }

        public Builder responseTimeMs(Long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }

        public TriageResponse build() {
            TriageResponse r = new TriageResponse();
            r.symptoms = symptoms;
            r.severity = severity;
            r.urgency = urgency;
            r.likelyCondition = likelyCondition;
            r.recommendedAction = recommendedAction;
            r.firstAidInstructions = firstAidInstructions;
            r.requiresEmergencyContact = requiresEmergencyContact;
            r.responseTimeMs = responseTimeMs;
            return r;
        }
    }
}
