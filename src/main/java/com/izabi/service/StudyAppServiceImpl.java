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
import com.izabi.mapper.StudyMaterialMapper;
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
    private final AIService aiService;
    private final QuestionGenerationService questionGenerationService;
    private final StudyMaterialRepository studyMaterialRepository;
    private final StudyQuestionRepository studyQuestionRepository;

    @Override
    public StudyMaterialResponse generateStudyMaterial(MultipartFile file) {
        if(file == null || file.isEmpty()){
            throw new NoFileFoundException("Extracted text is empty");
        }
        ReadDocumentResponse extracted = fileTextExtractionService.navigateToProperFileExtension(file);
        AnalyzedContentResponse analysisResponse = aiService.analyzeContent(extracted.getText());
        SummarizedContentResponse summaryResponse = aiService.summarizeContent(extracted.getText());
        List<StudyQuestionResponse> generatedQuestions =
                questionGenerationService.generateQuestionsFromFile(file.getOriginalFilename(), file);

        FileExtensionResponse fileExt = fileTextExtractionService.getFileExtension(file);
        PageCountResponse pageCount = fileTextExtractionService.getPageCount(file);

        StudyMaterial studyMaterial = StudyMaterialMapper
                .mapToStudyMaterial(file, fileExt,pageCount,extracted,summaryResponse,analysisResponse);
        studyMaterial = studyMaterialRepository.save(studyMaterial);

        List<StudyQuestion> questionList = new ArrayList<>();
        for (StudyQuestionResponse questions : generatedQuestions) {
            StudyQuestion entity = new StudyQuestion();
            entity.setStudyMaterialId(studyMaterial.getId());
            entity.setQuestion(questions.getQuestion());
            entity.setOptions(questions.getOptions());
            entity.setCorrectAnswer(questions.getAnswer());
            entity.setExplanation(questions.getExplanation());
            entity.setTopic(questions.getTopic());
            if (questions.getDifficulty() != null) {
                entity.setDifficulty(questions.getDifficulty());
            } else {
                entity.setDifficulty(Difficulty.BEGINNER);
            }
            if (questions.getQuestionType() != null) {
                entity.setQuestionType(questions.getQuestionType());
            } else {
                entity.setQuestionType(QuestionType.MULTIPLE_CHOICE);
            }
            entity.setActive(true);
            entity.setCreatedAt(LocalDateTime.now());

            questionList.add(entity);
        }

        studyQuestionRepository.saveAll(questionList);

        return StudyMaterialMapper.mapToStudyMaterialResponse(
                studyMaterial.getSummary(), studyMaterial.getKeyPoints(), questionList, "Generated Study Material"

        );
    }
}
