package com.izabi.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.izabi.data.enums.Difficulty;
import com.izabi.data.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudyQuestionResponse {
    private String id;
    private String userId;
    private String fileId;
    private String fileName;
    private String question;
    private QuestionType questionType;
    private List<String> options;
    private String answer;
    private String explanation;
    private String topic;
    private Difficulty difficulty;
    private boolean isActive;
    private LocalDateTime createdAt;
}
