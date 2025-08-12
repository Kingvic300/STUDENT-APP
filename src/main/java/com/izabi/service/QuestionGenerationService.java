package com.izabi.service;

import com.izabi.dto.StudyQuestionDTO;

import java.util.List;

public interface QuestionGenerationService {
    List<StudyQuestionDTO> generateQuestionsFromFile(String fileId, String extractedText);
}
