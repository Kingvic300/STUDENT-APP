package com.izabi.service;

import com.izabi.dto.request.StudyMaterialRequest;
import com.izabi.data.enums.Difficulty;
import com.izabi.data.enums.QuestionType;
import com.izabi.data.model.StudyMaterial;
import com.izabi.data.model.StudyQuestion;
import com.izabi.data.repository.StudyMaterialRepository;
import com.izabi.data.repository.StudyQuestionRepository;
import com.izabi.dto.response.*;
import com.izabi.exception.NoFileFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudyAppServiceImpl implements StudyAppService {

    private final FileTextExtractionService fileTextExtractionService;
    private final AIService AIService;
    private final QuestionGenerationService questionGenerationService;
    private final StudyMaterialRepository studyMaterialRepository;
    private final StudyQuestionRepository studyQuestionRepository;

    @Override
    public StudyMaterialRequest generateStudyMaterial(MultipartFile file) {
        if(file == null || file.isEmpty()){
            throw new NoFileFoundException("Extracted text is empty");
        }
        ReadDocumentResponse extracted = fileTextExtractionService.navigateToProperFileExtension(file);

        SummarizedContentResponse summaryResponse = AIService.summarizeContent(extracted.getText());

        AnalyzedContentResponse analysisResponse = AIService.analyzeContent(extracted.getText());

        List<StudyQuestionResponse> generatedQuestions =
                questionGenerationService.generateQuestionsFromFile(file.getOriginalFilename(), file);

        FileExtensionResponse fileExt = fileTextExtractionService.getFileExtension(file);
        PageCountResponse pageCount = fileTextExtractionService.getPageCount(file);

        StudyMaterial studyMaterial = new StudyMaterial();
        studyMaterial.setFileName(file.getOriginalFilename());
        studyMaterial.setFileExtension(fileExt.getFileExtension());
        studyMaterial.setNumberOfPages(pageCount.getNumberOfPages());
        studyMaterial.setExtractedText(extracted.getText());
        studyMaterial.setSummary(summaryResponse.getSummary());
        studyMaterial.setKeyPoints(List.of(analysisResponse.getAnalyzed()));
        studyMaterial.setActive(true);
        studyMaterial.setUploadDate(LocalDateTime.now());
        studyMaterial = studyMaterialRepository.save(studyMaterial);

        List<StudyQuestion> questionEntities = new ArrayList<>();
        for (StudyQuestionResponse q : generatedQuestions) {
            StudyQuestion entity = new StudyQuestion();
            entity.setStudyMaterialId(studyMaterial.getId());
            entity.setQuestion(q.getQuestion());
            entity.setOptions(q.getOptions());
            entity.setCorrectAnswer(q.getAnswer());
            entity.setExplanation(q.getExplanation());
            entity.setTopic(q.getTopic());
            entity.setDifficulty(q.getDifficulty() != null ? q.getDifficulty() : Difficulty.BEGINNER);
            entity.setQuestionType(q.getQuestionType() != null ? q.getQuestionType() : QuestionType.MULTIPLE_CHOICE);
            entity.setActive(true);
            entity.setCreatedAt(LocalDateTime.now());
            questionEntities.add(entity);
        }
        studyQuestionRepository.saveAll(questionEntities);

        return StudyMaterialRequest.builder()
                .summary(summaryResponse.getSummary())
                .keyPoints(List.of(analysisResponse.getAnalyzed()))
                .questions(generatedQuestions)
                .build();
    }
}
