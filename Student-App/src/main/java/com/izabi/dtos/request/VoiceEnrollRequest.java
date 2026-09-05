package com.izabi.dtos.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VoiceEnrollRequest {
    private String email;
    private MultipartFile voiceSample;
}
