package com.izabi.service;

import com.izabi.data.enums.Difficulty;
import com.izabi.data.enums.QuestionType;
import com.izabi.data.model.Pdf;
import com.izabi.data.model.StudyMaterial;
import com.izabi.data.model.StudyQuestion;
import com.izabi.data.model.User;
import com.izabi.data.repository.PdfRepository;
import com.izabi.data.repository.StudyMaterialRepository;
import com.izabi.data.repository.StudyQuestionRepository;
import com.izabi.data.repository.UserRepository;
import com.izabi.dtos.request.GenerateQuestionsRequest;
import com.izabi.dtos.request.GenerateStudyMaterialRequest;
import com.izabi.dtos.request.NavigateToProperFileExtensionRequest;
import com.izabi.dtos.request.SummarizeContentRequest;
import com.izabi.dtos.response.*;
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
    private final PdfRepository pdfRepository;

    @Override
    public SummarizedContentResponse summarizeFile(SummarizeContentRequest request) throws Exception {
        validateUserAndFile(request.getUserId(), request.getFile());

        NavigateToProperFileExtensionRequest navigateRequest = new NavigateToProperFileExtensionRequest();
        navigateRequest.setUserId(request.getUserId());
        navigateRequest.setFile(request.getFile());

        ReadDocumentResponse extracted = fileTextExtractionService.navigateToProperFileExtension(navigateRequest);
        return aiService.summarizeContent(extracted.getText());
    }

    @Override
    public List<StudyQuestionResponse> generateQuestions(GenerateQuestionsRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));

        Pdf pdf = pdfRepository.findById(request.getFileId())
                .orElseThrow(() -> new NoFileFoundException("File not found with id: " + request.getFileId()));

        return questionGenerationService.generateQuestionsFromFile(request);
    }

    @Override
    public StudyMaterialResponse generateStudyMaterial(GenerateStudyMaterialRequest request) throws Exception {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Pdf pdf = pdfRepository.findById(request.getFileId())
                .orElseThrow(() -> new NoFileFoundException("PDF not found with id: " + request.getFileId()));

        String fileContent = pdf.getContent();
        if (fileContent == null || fileContent.isBlank()) {
            throw new NoFileFoundException("No content found in the uploaded PDF");
        }

        AnalyzedContentResponse analysisResponse = aiService.analyzeContent(fileContent);
        SummarizedContentResponse summaryResponse = aiService.summarizeContent(fileContent);
        GetKeyPointsResponse keyPointsResponse = aiService.getKeyPoints(fileContent);

        // Generate question
        GenerateQuestionsRequest getQuestionsRequest = new GenerateQuestionsRequest();
        getQuestionsRequest.setUserId(user.getId());
        getQuestionsRequest.setFileId(pdf.getId());
        getQuestionsRequest.setNumberOfQuestions(request.getNumberOfQuestions());

        List<StudyQuestionResponse> generatedQuestions = questionGenerationService.generateQuestionsFromFile(getQuestionsRequest);

        // Create and save study material
        StudyMaterial studyMaterial = new StudyMaterial();
        studyMaterial.setId(pdf.getId());
        studyMaterial.setUserId(user.getId());
        studyMaterial.setFileName(pdf.getFileName());
        studyMaterial.setSummary(summaryResponse.getSummary());

        // Use the key points from the dedicated key points response
        studyMaterial.setKeyPoints(keyPointsResponse.getKeyPoints());

        studyMaterial.setCreatedAt(LocalDateTime.now());

        studyMaterialRepository.save(studyMaterial);

        // Save questions
        List<StudyQuestion> questionList = mapToEntities(generatedQuestions, pdf.getId(), user.getId());
        studyQuestionRepository.saveAll(questionList);

        // Prepare question responses
        List<StudyQuestionResponse> questionResponses = new ArrayList<>();
        for (StudyQuestion q : questionList) {
            questionResponses.add(getStudyQuestionResponse(q, user));
        }

        return StudyMaterialMapper.mapToStudyMaterialResponse(
                studyMaterial.getId(),
                studyMaterial.getFileName(),
                summaryResponse.getSummary(),
                studyMaterial.getKeyPoints(),
                questionResponses,
                studyMaterial.getCreatedAt(),
                "Generated Study Material"
        );
    }

    @Override
    public List<StudyMaterialResponse> getStudyHistory(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<StudyMaterial> materials = studyMaterialRepository.findByUserId(user.getId());
        List<StudyMaterialResponse> responses = new ArrayList<>();

        for (StudyMaterial material : materials) {
            List<StudyQuestion> questions = studyQuestionRepository.findByFileId(material.getId());
            List<StudyQuestionResponse> questionResponses = new ArrayList<>();

            for (StudyQuestion q : questions) {
                questionResponses.add(getStudyQuestionResponse(q, user));
            }

            StudyMaterialResponse response = StudyMaterialMapper.mapToStudyMaterialResponse(
                    material.getId(),
                    material.getFileName(),
                    material.getSummary(),
                    material.getKeyPoints(),
                    questionResponses,
                    material.getCreatedAt(),
                    "History Record"
            );
            responses.add(response);
        }

        return responses;
    }

    @Override
    public void deleteStudyMaterial(String studyMaterialId, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        StudyMaterial material = studyMaterialRepository.findById(studyMaterialId)
                .orElseThrow(() -> new NoFileFoundException("Study material not found"));

        if (!material.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("User not authorized to delete this study material");
        }

        studyQuestionRepository.deleteAll(studyQuestionRepository.findByFileId(material.getId()));
        studyMaterialRepository.delete(material);
    }

    private void validateUserAndFile(String userId, MultipartFile file) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        if (file == null || file.isEmpty()) {
            throw new NoFileFoundException("File is empty");
        }
    }

    private static StudyQuestionResponse getStudyQuestionResponse(StudyQuestion studyQuestion, User user) {
        StudyQuestionResponse response = new StudyQuestionResponse();
        response.setId(studyQuestion.getId());
        response.setUserId(user.getId());
        response.setQuestion(studyQuestion.getQuestion());
        response.setDifficulty(studyQuestion.getDifficulty());
        response.setOptions(studyQuestion.getOptions());
        response.setAnswer(studyQuestion.getAnswer());
        response.setTopic(studyQuestion.getTopic());
        response.setExplanation(studyQuestion.getExplanation());
        response.setFileName(studyQuestion.getFileName());
        response.setFileId(studyQuestion.getFileId());
        response.setQuestionType(studyQuestion.getQuestionType());
        response.setActive(studyQuestion.isActive());
        response.setCreatedAt(studyQuestion.getCreatedAt());
        return response;
    }

    private List<StudyQuestion> mapToEntities(List<StudyQuestionResponse> generatedQuestions,
                                              String studyMaterialId,
                                              String userId) {
        List<StudyQuestion> questionList = new ArrayList<>();
        for (StudyQuestionResponse q : generatedQuestions) {
            StudyQuestion question = new StudyQuestion();
            question.setFileId(studyMaterialId);
            question.setUserId(userId);
            question.setQuestion(q.getQuestion());
            question.setOptions(q.getOptions());
            question.setAnswer(q.getAnswer());
            question.setExplanation(q.getExplanation());
            question.setTopic(q.getTopic());
            question.setDifficulty(q.getDifficulty() != null ? q.getDifficulty() : Difficulty.BEGINNER);
            question.setQuestionType(q.getQuestionType() != null ? q.getQuestionType() : QuestionType.MULTIPLE_CHOICE);
            question.setActive(true);
            question.setCreatedAt(LocalDateTime.now());
            questionList.add(question);
        }
        return questionList;
    }
}