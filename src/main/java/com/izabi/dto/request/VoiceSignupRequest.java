package com.izabi.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VoiceSignupRequest {
    private String email;
    private String role;
    private MultipartFile voiceSample;
}

