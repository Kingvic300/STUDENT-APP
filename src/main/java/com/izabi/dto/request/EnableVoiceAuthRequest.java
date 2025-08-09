package com.izabi.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class EnableVoiceAuthRequest {
    private MultipartFile voiceSample;
}
