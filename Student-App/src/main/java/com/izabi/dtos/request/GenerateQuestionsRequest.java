package com.izabi.dtos.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class GenerateQuestionsRequest {
    private String userId;
    private String fileId;
    private int numberOfQuestions;
}
