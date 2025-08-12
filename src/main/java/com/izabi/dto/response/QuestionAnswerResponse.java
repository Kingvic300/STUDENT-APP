package com.izabi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswerResponse {
    private boolean isCorrect;
    private String correctAnswer;
    private String explanation;
    private String feedback;
    private double newSuccessRate;
}
