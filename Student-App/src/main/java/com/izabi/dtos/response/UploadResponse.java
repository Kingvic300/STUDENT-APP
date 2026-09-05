package com.izabi.dtos.response;

import lombok.Data;

@Data
public class UploadResponse {
    private String message;
    private String cloudinaryUrl;
}
