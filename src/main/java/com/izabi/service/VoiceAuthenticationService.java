package com.izabi.service;

import com.izabi.dto.response.EmbeddingResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface VoiceAuthenticationService {
    EmbeddingResponse extractVoiceFeatures(MultipartFile voiceSample) throws IOException;
    boolean verifyVoice(MultipartFile voiceSample, String storedVoicePrint) throws IOException;
    String generateSecurePassword();
}
