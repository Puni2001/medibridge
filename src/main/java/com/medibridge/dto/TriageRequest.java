package com.medibridge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TriageRequest {

    @NotBlank(message = "Voice text cannot be empty")
    @Size(max = 1000, message = "Voice text cannot exceed 1000 characters")
    private String voiceText;

    private String imageBase64;

    public String getVoiceText() {
        return voiceText;
    }

    public void setVoiceText(String voiceText) {
        this.voiceText = voiceText;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
