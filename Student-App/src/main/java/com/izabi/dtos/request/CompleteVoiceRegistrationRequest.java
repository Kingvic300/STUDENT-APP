package com.izabi.dtos.request;


import lombok.Data;

@Data
public class CompleteVoiceRegistrationRequest {
    private String email;
    private String otp;
}

