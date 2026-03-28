package com.medibridge.dto;

public class WoundAnalysis {
    private String description;
    private String severity;
    private String immediateCare;

    public WoundAnalysis() {}

    public WoundAnalysis(String description, String severity, String immediateCare) {
        this.description = description;
        this.severity = severity;
        this.immediateCare = immediateCare;
    }

    public static builder Builder() {
        return new builder();
    }

    public static class builder {
        private String description;
        private String severity;
        private String immediateCare;

        public builder description(String d) { this.description = d; return this; }
        public builder severity(String s) { this.severity = s; return this; }
        public builder immediateCare(String i) { this.immediateCare = i; return this; }
        public WoundAnalysis build() {
            return new WoundAnalysis(description, severity, immediateCare);
        }
    }

    public String getDescription() { return description; }
    public String getSeverity() { return severity; }
    public String getImmediateCare() { return immediateCare; }
}
