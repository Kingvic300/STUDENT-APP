package com.izabi.data.model;

import com.izabi.data.enums.Difficulty;
import com.izabi.data.enums.QuestionType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "studyQuestions")
public class StudyQuestion {
    @Id
    private String id;

    @Indexed
    private String pdfId;
    @Indexed
    private String userId;

    private String question;
    private QuestionType questionType;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private String topic;
    private Difficulty difficulty;
    private double aiConfidence;
    private int timesAnswered;
    private int timesCorrect;
    private double successRate;
    private LocalDateTime createdDate;
    private boolean isActive;
    private String lastUserAnswer;
    private boolean wasLastAnswerCorrect;
    private LocalDateTime lastAnsweredDate;
}
