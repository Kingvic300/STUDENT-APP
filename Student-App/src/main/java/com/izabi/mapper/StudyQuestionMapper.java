package com.izabi.mapper;

import com.izabi.data.model.StudyQuestion;
import com.izabi.dtos.response.StudyQuestionResponse;

public class StudyQuestionMapper {

    public static StudyQuestion mapToStudyQuestionEntity(StudyQuestionResponse response, String fileId, String userId) {
        return StudyQuestion.builder()
                .fileId(fileId)
                .userId(userId)
                .question(response.getQuestion())
                .questionType(response.getQuestionType())
                .options(response.getOptions())
                .answer(response.getAnswer())
                .difficulty(response.getDifficulty())
                .topic(response.getTopic())
                .explanation(response.getExplanation())
                .isActive(true)
                .createdAt(response.getCreatedAt() != null ? response.getCreatedAt() : java.time.LocalDateTime.now())
                .build();
    }

    public static StudyQuestionResponse mapToStudyQuestionResponse(StudyQuestion question) {
        return StudyQuestionResponse.builder()
                .id(question.getId())
                .fileId(question.getFileId())
                .userId(question.getUserId())
                .question(question.getQuestion())
                .questionType(question.getQuestionType())
                .options(question.getOptions())
                .answer(question.getAnswer())
                .difficulty(question.getDifficulty())
                .topic(question.getTopic())
                .explanation(question.getExplanation())
                .isActive(question.isActive())
                .createdAt(question.getCreatedAt())
                .build();
    }
}
