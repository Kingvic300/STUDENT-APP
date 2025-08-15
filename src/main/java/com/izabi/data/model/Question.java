package com.izabi.data.model;

import com.izabi.data.enums.Difficulty;
import com.izabi.data.enums.QuestionType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "questions")
public class Question {

    @Id
    private String id;

    private String fileId;

    @Field("question")
    private String question;

    private List<String> options;

    private String answer;

    private Difficulty difficulty;

    private QuestionType questionType;
}
