package com.izabi.dtos.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class GenerateStudyMaterialRequest {
    private String userId;
    private String fileId;
    private int numberOfQuestions;

}
