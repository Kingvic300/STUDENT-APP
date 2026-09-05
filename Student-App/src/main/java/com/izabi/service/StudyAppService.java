package com.izabi.service;

import com.izabi.dtos.request.GenerateQuestionsRequest;
import com.izabi.dtos.request.GenerateStudyMaterialRequest;
import com.izabi.dtos.request.SummarizeContentRequest;
import com.izabi.dtos.response.StudyMaterialResponse;
import com.izabi.dtos.response.StudyQuestionResponse;
import com.izabi.dtos.response.SummarizedContentResponse;

import java.util.List;

public interface StudyAppService {
    SummarizedContentResponse summarizeFile(SummarizeContentRequest request) throws Exception;

    List<StudyQuestionResponse> generateQuestions(GenerateQuestionsRequest request);

    StudyMaterialResponse generateStudyMaterial(GenerateStudyMaterialRequest request) throws Exception;

    List<StudyMaterialResponse> getStudyHistory(String userId);

    void deleteStudyMaterial(String studyMaterialId, String userId);
}