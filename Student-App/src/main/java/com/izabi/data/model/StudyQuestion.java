package com.izabi.data.model;

import com.izabi.data.enums.Difficulty;
import com.izabi.data.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "questions")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudyQuestion {

    @Id
    private String id;

    @Indexed
    private String fileId;

    private String fileName;

    @Indexed
    private String userId;

    private String question;

    private QuestionType questionType;

    private List<String> options;

    private String answer;

    private String explanation;

    private String topic;

    private Difficulty difficulty;

    private LocalDateTime createdAt;

    private boolean isActive;

}
