package com.izabi.service;

import com.izabi.dto.response.StudyMaterialResponse;
import com.izabi.dto.response.StudyQuestionResponse;
import com.izabi.dto.response.SummarizedContentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudyAppService {
    SummarizedContentResponse summarizeFile(MultipartFile file, String userId);

    List<StudyQuestionResponse> generateQuestions(MultipartFile file, String userId, int numberOfQuestions);

    StudyMaterialResponse generateStudyMaterial(MultipartFile file, String userId, int numberOfQuestions);

    List<StudyMaterialResponse> getStudyHistory(String userId);
}
