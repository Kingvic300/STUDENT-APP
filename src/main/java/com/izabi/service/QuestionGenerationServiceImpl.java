package com.izabi.service;

import com.izabi.dto.StudyQuestionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionGenerationServiceImpl implements QuestionGenerationService {

    private final FileTextExtractionService fileTextExtractionService;
    private final OpenAIService openAIService;

    @Override
    public List<StudyQuestionDTO> generateQuestionsFromFile(String fileId, String extractedText) {
        String questionsToGenerate;
        if (extractedText.length() > 3000) {
            questionsToGenerate = extractedText.substring(0, 3000) + "...";
        } else {
            questionsToGenerate = extractedText;
        }
        log.info("Generating questions for fileId: {}", fileId);

        String prompt = String.format("""
                You are an educational assistant.
                Based on the following content, generate a list of study questions with the following JSON format:

                [
                  {
                    "question": "string",
                    "options": ["option1", "option2", "option3", "option4"],
                    "answer": "string",
                    "difficulty": "BEGINNER|INTERMEDIATE|ADVANCED",
                    "questionType": "MULTIPLE_CHOICE|TRUE_FALSE|SHORT_ANSWER"
                  }
                ]

                Content:
                %s
                """, questionsToGenerate);

        Map<String, Object> aiResponse = openAIService.analyzeContent(prompt);

        List<StudyQuestionDTO> questions = new ArrayList<>();
        try {
            Object questionsObj = aiResponse.get("questions");
            if (!(questionsObj instanceof List)) {
                log.warn("Questions field is missing or not a list in AI response");
                return questions;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questionList = (List<Map<String, Object>>) questionsObj;

            for (Map<String, Object> q : questionList) {
                StudyQuestionDTO dto = parseSingleQuestion(q);
                if (dto != null) {
                    questions.add(dto);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing AI question generation response", e);
        }

        return questions;
    }

    private StudyQuestionDTO parseSingleQuestion(Map<String, Object> q) {
        try {
            StudyQuestionDTO dto = new StudyQuestionDTO();
            dto.setId(UUID.randomUUID().toString());

            Object question = q.get("question");
            if (question instanceof String) {
                dto.setQuestion((String) question);
            } else {
                log.warn("Invalid or missing question field");
                return null;
            }

            Object options = q.get("options");
            if (options instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> optionsList = (List<String>) options;
                dto.setOptions(optionsList);
            } else {
                log.warn("Invalid or missing options field");
                return null;
            }

            Object answer = q.get("answer");
            if (answer instanceof String) {
                dto.setCorrectAnswer((String) answer);
            } else {
                log.warn("Invalid or missing answer field");
                return null;
            }

            Object difficulty = q.get("difficulty");
            if (difficulty instanceof String) {
                dto.setDifficulty((String) difficulty);
            } else {
                log.warn("Invalid or missing difficulty field");
                return null;
            }

            Object questionType = q.get("questionType");
            if (questionType instanceof String) {
                dto.setQuestionType((String) questionType);
            } else {
                log.warn("Invalid or missing questionType field");
                return null;
            }

            return dto;
        } catch (Exception e) {
            log.error("Error parsing single question", e);
            return null;
        }
    }
}