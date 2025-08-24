package com.izabi.service;

import com.izabi.data.enums.Difficulty;
import com.izabi.data.enums.QuestionType;
import com.izabi.data.model.StudyMaterial;
import com.izabi.data.model.StudyQuestion;
import com.izabi.data.model.User;
import com.izabi.data.repository.StudyMaterialRepository;
import com.izabi.data.repository.StudyQuestionRepository;
import com.izabi.data.repository.UserRepository;
import com.izabi.dto.response.*;
import com.izabi.exception.NoFileFoundException;
import com.izabi.exception.UserNotFoundException;
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
    private final UserRepository userRepository;
    private final QuestionGenerationService questionGenerationService;
    private final StudyMaterialRepository studyMaterialRepository;
    private final StudyQuestionRepository studyQuestionRepository;

    @Override
    public SummarizedContentResponse summarizeFile(MultipartFile file, String userId) {
        validateUserAndFile(file, userId);

        ReadDocumentResponse extracted = fileTextExtractionService.navigateToProperFileExtension(file);

        return aiService.summarizeContent(extracted.getText());
    }

    @Override
    public List<StudyQuestionResponse> generateQuestions(MultipartFile file, String userId, int numberOfQuestions) {
        validateUserAndFile(file, userId);

        // Call service that generates N questions
        return questionGenerationService.generateQuestionsFromFile(
                file.getOriginalFilename(),
                file,
                numberOfQuestions
        );
    }

    @Override
    public StudyMaterialResponse generateStudyMaterial(MultipartFile file, String userId, int numberOfQuestions) {
        if (file == null || file.isEmpty()) {
            throw new NoFileFoundException("Extracted text is empty");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        ReadDocumentResponse extracted = fileTextExtractionService.navigateToProperFileExtension(file);
        AnalyzedContentResponse analysisResponse = aiService.analyzeContent(extracted.getText());
        SummarizedContentResponse summaryResponse = aiService.summarizeContent(extracted.getText());

        List<StudyQuestionResponse> generatedQuestions =
                questionGenerationService.generateQuestionsFromFile(file.getOriginalFilename(), file, numberOfQuestions);

        FileExtensionResponse fileExt = fileTextExtractionService.getFileExtension(file);
        PageCountResponse pageCount = fileTextExtractionService.getPageCount(file);

        StudyMaterial studyMaterial = StudyMaterialMapper
                .mapToStudyMaterial(file, fileExt, pageCount, extracted, summaryResponse, analysisResponse);
        studyMaterialRepository.save(studyMaterial);

        // Map and save questions
        List<StudyQuestion> questionList = mapToEntities(generatedQuestions, studyMaterial.getId(), user.getId());
        studyQuestionRepository.saveAll(questionList);

        return StudyMaterialMapper.mapToStudyMaterialResponse(
                summaryResponse.getSummary(), studyMaterial.getKeyPoints(), questionList, "Generated Study Material"
        );
    }
    @Override
    public List<StudyMaterialResponse> getStudyHistory(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<StudyMaterial> studyMaterials = studyMaterialRepository.findByUserId(user.getId());

        List<StudyMaterialResponse> responses = new ArrayList<>();

        for (StudyMaterial material : studyMaterials) {
            List<StudyQuestion> questions = studyQuestionRepository.findByStudyMaterialId(material.getId());

            StudyMaterialResponse response = StudyMaterialMapper.mapToStudyMaterialResponse(
                    material.getSummary(),
                    material.getKeyPoints(),
                    questions,
                    "History Record"
            );
            responses.add(response);
        }

        return responses;
    }


    private void validateUserAndFile(MultipartFile file, String userId) {
        if (file == null || file.isEmpty()) {
            throw new NoFileFoundException("File is empty");
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
    }

    private List<StudyQuestion> mapToEntities(List<StudyQuestionResponse> generatedQuestions, String studyMaterialId, String userId) {
        List<StudyQuestion> questionList = new ArrayList<>();
        for (StudyQuestionResponse q : generatedQuestions) {
            StudyQuestion entity = new StudyQuestion();
            entity.setStudyMaterialId(studyMaterialId);
            entity.setUserId(userId);
            entity.setQuestion(q.getQuestion());
            entity.setOptions(q.getOptions());
            entity.setCorrectAnswer(q.getAnswer());
            entity.setExplanation(q.getExplanation());
            entity.setTopic(q.getTopic());
            entity.setDifficulty(q.getDifficulty() != null ? q.getDifficulty() : Difficulty.BEGINNER);
            entity.setQuestionType(q.getQuestionType() != null ? q.getQuestionType() : QuestionType.MULTIPLE_CHOICE);
            entity.setActive(true);
            entity.setCreatedAt(LocalDateTime.now());
            questionList.add(entity);
        }
        return questionList;
    }
}
