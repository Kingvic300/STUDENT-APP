package com.izabi.dto.response;

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
public class StudyQuestionResponse {
    private String id;
    private String question;
    private QuestionType questionType;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private String topic;
    private Difficulty difficulty;
    private LocalDateTime createdAt;
}
