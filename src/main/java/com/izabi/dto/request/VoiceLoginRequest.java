package com.izabi.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VoiceLoginRequest {
    private String email;
    private MultipartFile voiceSample;
}
