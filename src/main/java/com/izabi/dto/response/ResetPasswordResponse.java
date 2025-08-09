package com.izabi.dto.response;

import lombok.Data;

@Data
public class ResetPasswordResponse {
    private String message;
    private String email;
}
