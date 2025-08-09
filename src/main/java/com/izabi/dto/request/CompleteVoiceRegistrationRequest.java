package com.izabi.dto.request;


import lombok.Data;

@Data
public class CompleteVoiceRegistrationRequest {
    private String email;
    private String otp;
}

