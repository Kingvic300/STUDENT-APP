package com.izabi.service;

import com.izabi.dto.StudyMaterialDTO;
import com.izabi.dto.StudyQuestionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudyAppServiceImpl implements StudyAppService {

    private final FileTextExtractionService fileTextExtractionService;
    private final OpenAIService openAIService;
    private final QuestionGenerationService questionGenerationService;


    @Override
    public StudyMaterialDTO generateStudyMaterial(MultipartFile file) {
        String extractedText = fileTextExtractionService.navigateToProperFileExtension(file);

        Map<String, Object> summaryResult = openAIService.summarizeContent(extractedText);

        List<StudyQuestionDTO> questions = questionGenerationService.generateQuestionsFromFile(
                file.getOriginalFilename(), extractedText
        );

        StudyMaterialDTO studyMaterial = new StudyMaterialDTO();
        studyMaterial.setSummary(String.valueOf(summaryResult.get("summary")));
        studyMaterial.setKeyPoints(List.of(summaryResult.get("keyPoints").toString()));
        studyMaterial.setQuestions(questions);

        return studyMaterial;
    }
}
