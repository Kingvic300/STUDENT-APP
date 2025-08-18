package com.izabi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izabi.data.model.Question;
import com.izabi.dto.response.ReadDocumentResponse;
import com.izabi.dto.response.StudyQuestionResponse;
import com.izabi.data.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionGenerationServiceImpl implements QuestionGenerationService {

    private final AIService AIService;
    private final FileTextExtractionService fileTextExtractionService;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<StudyQuestionResponse> generateQuestionsFromFile(String fileId, MultipartFile file) {
        try {
            ReadDocumentResponse readResponse = fileTextExtractionService.navigateToProperFileExtension(file);
            String extractedText = readResponse.getText();

            String aiResponse = AIService.generateQuestions(extractedText);

            List<StudyQuestionResponse> studyQuestions = objectMapper.readValue(
                    aiResponse,
                    new TypeReference<>() {
                    }
            );

            List<Question> questionEntities = studyQuestions.stream()
                    .map(sq -> Question.builder()
                            .fileId(fileId)
                            .question(sq.getQuestion())
                            .options(sq.getOptions())
                            .answer(sq.getAnswer())
                            .difficulty(sq.getDifficulty())
                            .questionType(sq.getQuestionType())
                            .build()
                    )
                    .collect(Collectors.toList());

            questionRepository.saveAll(questionEntities);

            log.info("Saved {} questions for fileId {}", questionEntities.size(), fileId);
            return studyQuestions;
        } catch (Exception e) {
            log.error("Failed to generate questions for fileId {}: {}", fileId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate questions", e);
        }
    }

    @Override
    public List<StudyQuestionResponse> findQuestions(String fileId) {
        return questionRepository.findByFileId(fileId).stream()
                .map(q -> StudyQuestionResponse.builder()
                        .id(q.getId())
                        .question(q.getQuestion())
                        .questionType(q.getQuestionType())
                        .options(q.getOptions())
                        .answer(q.getAnswer())
                        .explanation(null)
                        .topic(null)
                        .difficulty(q.getDifficulty())
                        .createdAt(LocalDateTime.now())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteQuestions(String fileId) {
        if (!questionRepository.findByFileId(fileId).isEmpty()) {
            questionRepository.deleteByFileId(fileId);
            log.info("Deleted questions for fileId {}", fileId);
            return true;
        }
        return false;
    }
}
