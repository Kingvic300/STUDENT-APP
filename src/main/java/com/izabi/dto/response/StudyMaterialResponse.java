package com.izabi.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data

public class StudyMaterialResponse {
    private String id;
    private String fileName;
    private String summary;
    private List<String> keyPoints;
    private List<QuestionResponse> questions;
    private LocalDateTime createdAt;
    private String message;

}
