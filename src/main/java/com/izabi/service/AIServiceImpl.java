package com.izabi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.izabi.mapper.StudyMaterialMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izabi.exception.*;
import com.izabi.dto.response.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Service
@Slf4j
public class AIServiceImpl implements AIService {
    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AnalyzedContentResponse analyzeContent(String text) {
        try {
            log.info("Starting OpenAI analysis for Computer Science content");

            String prompt = createAnalysisPrompt(text);
            String response = callOpenAI(prompt);
            String analyzed = parseAnalysisResponse(response);

            return StudyMaterialMapper.mapToAnalyzedContentResponse(analyzed, "Content successfully analyzed");

        } catch (Exception e) {
            log.error("OpenAI analysis failed: {}", e.getMessage());
            throw new AIAnalysisException("Failed to analyze content with OpenAI: " + e.getMessage(), e);
        }
    }

    @Override
    public SummarizedContentResponse summarizeContent(String text) {
        try {
            log.info("Starting OpenAI summarization");

            String prompt = createSummaryPrompt(text);
            String response = callOpenAI(prompt);
            String summary = parseSummaryResponse(response);

            return StudyMaterialMapper.mapToSummarizedContentResponse(summary,"Summary Completed");

        } catch (Exception e) {
            log.error("OpenAI summarization failed: {}", e.getMessage());
            throw new AIAnalysisException("Failed to summarize content with OpenAI: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateQuestions(String text) {
        try {
            log.info("Starting OpenAI question generation");

            String prompt = createQuestionGenerationPrompt(text);
            String response = callOpenAI(prompt);

            return extractContentFromOpenAIResponse(response);

        } catch (Exception e) {
            log.error("OpenAI question generation failed: {}", e.getMessage());
            throw new AIAnalysisException("Failed to generate questions with OpenAI: " + e.getMessage(), e);
        }
    }

    private String createSummaryPrompt(String text) {
        String contentToSummarize;
        if (text.length() > 3000) {
            contentToSummarize = text.substring(0, 3000) + "...";
        } else {
            contentToSummarize = text;
        }

        return String.format("""
        Summarize the following educational content in 3–5 concise sentences.
        Return the result as a JSON object with this structure:
        {
            "summary": "The concise summary of the content"
        }
        
        Content to summarize:
        %s
        """, contentToSummarize);
    }

    private String createQuestionGenerationPrompt(String text) {
        String contentToUse;
        if (text.length() > 3000) {
            contentToUse = text.substring(0, 3000) + "...";
        } else {
            contentToUse = text;
        }

        return String.format("""
        Generate a list of 5–10 study questions based on the following educational content.
        Return the result strictly as a JSON array of objects with this structure:
        [
            {
                "question": "The study question text",
                "options": ["Option A", "Option B", "Option C", "Option D"],
                "answer": "The correct option"
            }
        ]
        
        Content:
        %s
        """, contentToUse);
    }

    private String extractContentFromOpenAIResponse(String apiResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(apiResponse);
            return jsonNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Failed to parse OpenAI response body", e);
            throw new AIAnalysisException("Invalid OpenAI API response format", e);
        }
    }

    private String parseSummaryResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            String content = jsonNode.path("choices").get(0).path("message").path("content").asText();

            JsonNode contentNode = objectMapper.readTree(content);
            return contentNode.path("summary").asText();
        } catch (Exception e) {
            log.warn("Failed to parse OpenAI summary response, returning fallback summary: {}", e.getMessage());
            return "A brief summary of the educational content.";
        }
    }


    private String callOpenAI(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("max_tokens", 1000);
        requestBody.put("temperature", 0.3);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(openAiApiUrl, request, String.class);

        return response.getBody();
    }

    private String parseAnalysisResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);

            return jsonNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText(); // Returning raw JSON string from OpenAI

        } catch (JsonProcessingException e) {
            log.warn("Failed to parse OpenAI analysis response (JSON error), using fallback: {}", e.getMessage());
            try {
                return objectMapper.writeValueAsString(createFallbackAnalysis());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException("Failed to serialize fallback analysis", ex);
            }
        } catch (Exception e) {
            log.warn("Unexpected error parsing OpenAI analysis response, using fallback: {}", e.getMessage());
            try {
                return objectMapper.writeValueAsString(createFallbackAnalysis());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException("Failed to serialize fallback analysis", ex);
            }
        }
    }



    private Map<String, Object> createFallbackAnalysis() {
        return Map.of(
                "summary", "Educational content covering a range of concepts relevant to the subject matter.",
                "keyTopics", Arrays.asList("Topic1", "Topic2", "Topic3"),
                "keyTerms", Arrays.asList("Term1", "Term2", "Term3", "Term4", "Term5"),
                "difficulty", "INTERMEDIATE",
                "estimatedReadingTimeMinutes", 15,
                "courseSubject", "General Education",
                "contentType", "LECTURE_NOTES"
        );
    }

    private String createAnalysisPrompt(String text) {
        String contentToAnalyze;

        if (text.length() > 3000) {
            contentToAnalyze = text.substring(0, 3000) + "...";
        } else {
            contentToAnalyze = text;
        }

        return String.format("""
        Analyze this educational content and provide a JSON response with the following structure:
        
        {
            "summary": "A concise 2-3 sentence summary of the main concepts",
            "keyTopics": ["topic1", "topic2", "topic3"],
            "keyTerms": ["term1", "term2", "term3", "term4", "term5"],
            "difficulty": "BEGINNER|INTERMEDIATE|ADVANCED",
            "estimatedReadingTimeMinutes": number,
            "courseSubject": "The main subject area of the content",
            "contentType": "LECTURE_NOTES|TEXTBOOK|ASSIGNMENT"
        }
        Make sure you give accurate information.
        Content to analyze:
        %s
        """, contentToAnalyze);
    }
}
