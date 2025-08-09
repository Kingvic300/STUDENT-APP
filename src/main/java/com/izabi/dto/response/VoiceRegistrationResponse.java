package com.izabi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoiceRegistrationResponse {
    private String message;
    private String email;
    private String status;
}