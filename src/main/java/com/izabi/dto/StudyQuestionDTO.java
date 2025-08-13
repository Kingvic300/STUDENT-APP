package com.izabi.dto;

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
public class StudyQuestionDTO {
    private String id;
    private String question;
    private String questionType;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private String topic;
    private String difficulty;
    private double aiConfidence;
    private int timesAnswered;
    private LocalDateTime createdAt;
    private double successRate;
}
