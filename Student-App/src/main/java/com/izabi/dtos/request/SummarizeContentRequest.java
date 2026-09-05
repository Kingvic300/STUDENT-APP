package com.izabi.dtos.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SummarizeContentRequest {
    private String userId;
    private MultipartFile file;
}
