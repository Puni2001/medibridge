package com.medibridge.services;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TTSService {
    
    public byte[] generateVoiceInstructions(String text, String language) {
        try (TextToSpeechClient client = TextToSpeechClient.create()) {
            // Set the text input
            SynthesisInput input = SynthesisInput.newBuilder()
                .setText(text)
                .build();
            
            // Set voice parameters
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(getLanguageCode(language))
                .setName(getVoiceName(language))
                .build();
            
            // Set audio configuration
            AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .setSpeakingRate(0.9) // Slightly slower for emergencies
                .build();
            
            // Perform synthesis
            SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);
            
            // Get audio content
            ByteString audioContents = response.getAudioContent();
            return audioContents.toByteArray();
            
        } catch (Exception e) {
            log.error("TTS failed: {}", e.getMessage());
            return null;
        }
    }
    
    private String getLanguageCode(String language) {
        return switch (language.toLowerCase()) {
            case "es" -> "es-ES";
            case "fr" -> "fr-FR";
            case "de" -> "de-DE";
            case "hi" -> "hi-IN";
            case "zh" -> "zh-CN";
            default -> "en-US";
        };
    }
    
    private String getVoiceName(String language) {
        return switch (language.toLowerCase()) {
            case "es" -> "es-ES-Standard-A";
            case "fr" -> "fr-FR-Standard-A";
            case "de" -> "de-DE-Standard-A";
            case "hi" -> "hi-IN-Standard-A";
            case "zh" -> "zh-CN-Standard-A";
            default -> "en-US-Neural2-J"; // Calm, professional female voice
        };
    }
}
