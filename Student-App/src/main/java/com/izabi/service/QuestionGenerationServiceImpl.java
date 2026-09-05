package com.izabi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izabi.data.model.Pdf;
import com.izabi.data.model.StudyQuestion;
import com.izabi.data.model.User;
import com.izabi.data.repository.PdfRepository;
import com.izabi.data.repository.StudyQuestionRepository;
import com.izabi.data.repository.UserRepository;
import com.izabi.dtos.request.GenerateQuestionsRequest;
import com.izabi.dtos.response.StudyQuestionResponse;
import com.izabi.exception.UserNotFoundException;
import com.izabi.mapper.StudyQuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionGenerationServiceImpl implements QuestionGenerationService {

    private final AIService aiService;
    private final PdfRepository pdfRepository;
    private final FileTextExtractionService fileTextExtractionService;
    private final StudyQuestionRepository questionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;

    @Override
    public List<StudyQuestionResponse> generateQuestionsFromFile(GenerateQuestionsRequest request) {
        try {
            Pdf pdf = pdfRepository.findById(request.getFileId())
                    .orElseThrow(() -> new IllegalStateException("PDF file not found with id: " + request.getFileId()));

            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));

            String extractedText = pdf.getContent();
            if (extractedText == null || extractedText.isBlank()) {
                throw new IllegalStateException("No content found in PDF file: " + pdf.getFileName());
            }

            String aiResponse = aiService.generateQuestions(extractedText, request.getNumberOfQuestions());

            List<StudyQuestionResponse> studyQuestions = objectMapper.readValue(
                    aiResponse,
                    new TypeReference<>() {
                    }
            );

            List<StudyQuestion> questionEntities = studyQuestions.stream()
                    .map(q -> StudyQuestionMapper.mapToStudyQuestionEntity(q, request.getFileId(), request.getUserId()))
                    .collect(Collectors.toList());

            questionRepository.saveAll(questionEntities);

            log.info("✅ Saved {} generated questions for fileId {}", questionEntities.size(), request.getFileId());
            return studyQuestions;

        } catch (Exception e) {
            log.error("❌ Failed to generate questions for fileId {}: {}", request.getFileId(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate questions", e);
        }
    }

    @Override
    public List<StudyQuestionResponse> findQuestions(String fileId) {
        return questionRepository.findByFileId(fileId).stream()
                .map(StudyQuestionMapper::mapToStudyQuestionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteQuestions(String fileId) {
        var questions = questionRepository.findByFileId(fileId);
        if (!questions.isEmpty()) {
            questionRepository.deleteByFileId(fileId);
            log.info("🗑 Deleted {} questions for fileId {}", questions.size(), fileId);
            return true;
        }
        log.warn("⚠ No questions found for fileId {}", fileId);
        return false;
    }
}
