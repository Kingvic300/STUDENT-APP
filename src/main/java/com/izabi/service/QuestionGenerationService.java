package com.izabi.service;

import com.izabi.dto.response.StudyQuestionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuestionGenerationService {
    List<StudyQuestionResponse> generateQuestionsFromFile(String fileId, MultipartFile file, int numberOfQuestions);

    List<StudyQuestionResponse> findQuestions(String fileId);

    boolean deleteQuestions(String fileId);
}
