package com.izabi.service;

import com.izabi.dtos.request.GenerateQuestionsRequest;
import com.izabi.dtos.response.StudyQuestionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuestionGenerationService {
    List<StudyQuestionResponse> generateQuestionsFromFile(GenerateQuestionsRequest request);

    List<StudyQuestionResponse> findQuestions(String fileId);

    boolean deleteQuestions(String fileId);
}
