package com.izabi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izabi.dtos.response.AnalyzedContentResponse;
import com.izabi.dtos.response.GetKeyPointsResponse;
import com.izabi.dtos.response.SummarizedContentResponse;
import com.izabi.exception.AIAnalysisException;
import com.izabi.mapper.StudyMaterialMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${app.ai.max-content-length:4000}")
    private int maxContentLength;

    @Value("${app.ai.enable-fallbacks:true}")
    private boolean enableFallbacks;

    @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String geminiBaseUrl;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Override
    @Retryable(retryFor = {Exception.class}, maxAttemptsExpression = "3", backoff = @Backoff(delay = 1000, multiplier = 2))
    public AnalyzedContentResponse analyzeContent(String text) {
        try {
            log.info("Starting Gemini AI analysis for content (length: {} chars)", text.length());
            if (text.trim().isEmpty()) {
                throw new AIAnalysisException("Content cannot be empty");
            }
            String prompt = createAnalysisPrompt(text);
            String response = callGeminiAPI(prompt);
            String analyzed = parseAnalysisResponse(response);

            log.info("Gemini AI analysis completed successfully");
            return StudyMaterialMapper.mapToAnalyzedContentResponse(analyzed, "Content successfully analyzed");
        } catch (Exception e) {
            log.error("Gemini AI analysis failed: {}", e.getMessage());

            if (isQuotaError(e)) {
                throw new AIAnalysisException("Gemini AI service quota exceeded. Please check your Google Cloud billing and try again later.", e);
            }
            if (enableFallbacks) {
                log.warn("Using fallback analysis due to error: {}", e.getMessage());
                return createFallbackAnalyzedResponse();
            }
            throw new AIAnalysisException("Failed to analyze content: " + e.getMessage(), e);
        }
    }

    @Override
    @Retryable(retryFor = {Exception.class}, maxAttemptsExpression = "3", backoff = @Backoff(delay = 1000, multiplier = 2))
    public SummarizedContentResponse summarizeContent(String text) {
        try {
            log.info("Starting Gemini AI summarization (length: {} chars)", text.length());

            if (text.trim().isEmpty()) {
                throw new AIAnalysisException("Content cannot be empty");
            }

            String prompt = createSummaryPrompt(text);
            String response = callGeminiAPI(prompt);
            String summary = parseSummaryResponse(response);

            log.info("Gemini AI summarization completed successfully");
            return StudyMaterialMapper.mapToSummarizedContentResponse(summary, "Summary Completed");
        } catch (Exception e) {
            log.error("Gemini AI summarization failed: {}", e.getMessage());

            if (isQuotaError(e)) {
                throw new AIAnalysisException("Gemini AI service quota exceeded. Please check your Google Cloud billing and try again later.", e);
            }

            if (enableFallbacks) {
                log.warn("Using fallback summary due to error: {}", e.getMessage());
                String fallbackSummary = "Unable to generate AI summary at this time. The content appears to contain educational material that requires manual review.";
                return StudyMaterialMapper.mapToSummarizedContentResponse(fallbackSummary, "Fallback summary generated");
            }

            throw new AIAnalysisException("Failed to summarize content: " + e.getMessage(), e);
        }
    }

    @Override
    @Retryable(retryFor = {Exception.class}, maxAttemptsExpression = "3", backoff = @Backoff(delay = 1000, multiplier = 2))
    public GetKeyPointsResponse getKeyPoints(String text) {
        try {
            log.info("Starting Gemini AI key points extraction (length: {} chars)", text.length());

            if (text.trim().isEmpty()) {
                throw new AIAnalysisException("Content cannot be empty");
            }

            String prompt = createKeyPointsPrompt(text);
            String response = callGeminiAPI(prompt);
            List<String> keyPoints = parseKeyPointsResponse(response);

            log.info("Gemini AI key points extraction completed successfully");
            return StudyMaterialMapper.mapToGetKeyPointsResponse(keyPoints, "Key points extracted successfully");
        } catch (Exception e) {
            log.error("Gemini AI key points extraction failed: {}", e.getMessage());

            if (isQuotaError(e)) {
                throw new AIAnalysisException("Gemini AI service quota exceeded. Please check your Google Cloud billing and try again later.", e);
            }

            if (enableFallbacks) {
                log.warn("Using fallback key points due to error: {}", e.getMessage());
                List<String> fallbackKeyPoints = Arrays.asList(
                        "Main concepts and themes from the content",
                        "Important definitions and terminology",
                        "Key principles and methodologies discussed",
                        "Critical insights and conclusions"
                );
                return StudyMaterialMapper.mapToGetKeyPointsResponse(fallbackKeyPoints, "Fallback key points generated");
            }

            throw new AIAnalysisException("Failed to extract key points: " + e.getMessage(), e);
        }
    }

    @Retryable(retryFor = {Exception.class}, maxAttemptsExpression = "3", backoff = @Backoff(delay = 1000, multiplier = 2))
    @Override
    public String generateQuestions(String text, int numberOfQuestions) {
        try {
            log.info("Starting Gemini AI question generation (length: {} chars) for {} questions", text.length(), numberOfQuestions);

            if (text.trim().isEmpty()) {
                throw new AIAnalysisException("Content cannot be empty");
            }

            String prompt = createEnhancedQuestionGenerationPrompt(text, numberOfQuestions);
            String response = callGeminiAPI(prompt);
            String questions = parseEnhancedQuestionResponse(response);

            log.info("Gemini AI question generation completed successfully");
            return questions;
        } catch (Exception e) {
            log.error("Gemini AI question generation failed: {}", e.getMessage());

            if (isQuotaError(e)) {
                throw new AIAnalysisException("Gemini AI service quota exceeded. Please check your Google Cloud billing and try again later.", e);
            }

            if (enableFallbacks) {
                log.warn("Using fallback questions due to error: {}", e.getMessage());
                return createEnhancedFallbackQuestions();
            }

            throw new AIAnalysisException("Failed to generate questions: " + e.getMessage(), e);
        }
    }

    private String createKeyPointsPrompt(String text) {
        String contentToUse = truncateContent(text);
        return String.format("""
            You are an expert educational content analyzer. Extract the most important key points from the following educational content.
            
            Return ONLY a JSON object in this format:
            {
                "keyPoints": ["Key point 1", "Key point 2", "Key point 3", "Key point 4", "Key point 5"]
            }
            
            Guidelines:
            - Extract 5-7 most important key points
            - Focus on main concepts, definitions, and critical insights
            - Keep each key point concise but meaningful
            - Ensure key points are standalone and understandable
            
            Content:
            %s
            """, contentToUse);
    }

    private List<String> parseKeyPointsResponse(String response) {
        try {
            String cleaned = cleanJsonResponse(response);
            JsonNode node = objectMapper.readTree(cleaned);
            JsonNode keyPointsNode = node.path("keyPoints");

            if (!keyPointsNode.isArray() || keyPointsNode.isEmpty()) {
                throw new RuntimeException("Invalid key points format");
            }

            List<String> keyPoints = new ArrayList<>();
            for (JsonNode keyPoint : keyPointsNode) {
                keyPoints.add(keyPoint.asText());
            }

            return keyPoints;
        } catch (Exception e) {
            if (enableFallbacks) {
                return Arrays.asList(
                        "Main concepts and themes",
                        "Important definitions",
                        "Key principles",
                        "Critical insights"
                );
            }
            throw new AIAnalysisException("Failed to parse key points response", e);
        }
    }

    private String createEnhancedQuestionGenerationPrompt(String text, int numberOfQuestions) {
        String contentToUse = truncateContent(text);

        return "You are an expert educator. Generate " + numberOfQuestions + " study questions based on the following educational content.\n"
                + "Create a mix of MULTIPLE_CHOICE and THEORY questions that test understanding of key concepts.\n\n"
                + "For MULTIPLE_CHOICE questions: Include 4 options with one correct answer and explanation.\n"
                + "For THEORY questions: Create open-ended questions that require detailed explanations.\n\n"
                + "IMPORTANT: Return ONLY a JSON array with this exact structure (no additional text, no markdown formatting):\n"
                + "[\n"
                + "    {\n"
                + "        \"type\": \"MULTIPLE_CHOICE\",\n"
                + "        \"question\": \"The study question text\",\n"
                + "        \"topic\": \"The main topic this question covers\",\n"
                + "        \"difficulty\": \"BEGINNER or INTERMEDIATE or ADVANCED\",\n"
                + "        \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n"
                + "        \"answer\": \"The correct option (must match one of the options exactly)\",\n"
                + "        \"explanation\": \"Detailed explanation of why this is the correct answer and why other options are incorrect\"\n"
                + "    },\n"
                + "    {\n"
                + "        \"type\": \"THEORY\",\n"
                + "        \"question\": \"The open-ended theory question\",\n"
                + "        \"topic\": \"The main topic this question covers\",\n"
                + "        \"difficulty\": \"BEGINNER or INTERMEDIATE or ADVANCED\",\n"
                + "        \"explanation\": \"Sample answer or key points that should be covered in a good response\"\n"
                + "    }\n"
                + "]\n\n"
                + "Guidelines:\n"
                + "- Mix both question types (aim for 60% MULTIPLE_CHOICE, 40% THEORY)\n"
                + "- Ensure questions test understanding, not just memorization\n"
                + "- Difficulty should match the complexity of the concept being tested\n"
                + "- Topics should be specific to the content areas\n"
                + "- Explanations should be educational and comprehensive\n\n"
                + "Content:\n"
                + contentToUse;
    }

    // ... rest of your existing methods remain the same (callGeminiAPI, extractGeminiResponse, etc.)

    private String callGeminiAPI(String prompt) {
        String url = String.format("%s/models/%s:generateContent?key=%s",
                geminiBaseUrl, geminiModel, geminiApiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = getMapHttpEntity(prompt, headers);

        try {
            log.debug("Calling Gemini API with prompt length: {}", prompt.length());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return extractGeminiResponse(response.getBody());
            } else {
                throw new RuntimeException("Gemini API returned status: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            log.error("Gemini API HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            switch (e.getStatusCode()) {
                case HttpStatus.TOO_MANY_REQUESTS -> throw new RuntimeException("Gemini API rate limit exceeded", e);
                case HttpStatus.FORBIDDEN ->
                        throw new RuntimeException("Gemini API access denied - check API key and permissions", e);
                case HttpStatus.BAD_REQUEST -> throw new RuntimeException("Invalid request to Gemini API", e);
                default -> throw new IllegalStateException("Unexpected value: " + e.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }

    private static HttpEntity<Map<String, Object>> getMapHttpEntity(String prompt, HttpHeaders headers) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 4096,
                        "topP", 0.8,
                        "topK", 10
                ),
                "safetySettings", List.of(
                        Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                        Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                        Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                        Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE")
                )
        );

        return new HttpEntity<>(requestBody, headers);
    }

    private String extractGeminiResponse(String response) {
        try {
            JsonNode responseNode = objectMapper.readTree(response);

            if (responseNode.has("error")) {
                JsonNode error = responseNode.get("error");
                throw new RuntimeException("Gemini API error: " + error.path("message").asText());
            }

            JsonNode candidates = responseNode.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode first = candidates.get(0);
                JsonNode textNode = first.path("content").path("parts").get(0).path("text");
                return textNode.asText().trim();
            }

            throw new RuntimeException("No valid response from Gemini API");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Gemini API response", e);
        }
    }

    private String truncateContent(String text) {
        if (text.length() <= maxContentLength) return text;
        log.info("Truncating content from {} to {}", text.length(), maxContentLength);
        return text.substring(0, maxContentLength) + "...";
    }

    private String createSummaryPrompt(String text) {
        String content = truncateContent(text);
        return String.format("""
            You are an expert educational content summarizer. Summarize the following educational content in 3–5 concise sentences.

            Return ONLY a JSON object:
            {
                "summary": "The concise summary of the content"
            }

            Content:
            %s
            """, content);
    }

    private String createAnalysisPrompt(String text) {
        String contentToAnalyze = truncateContent(text);
        return String.format("""
        You are an expert educational content analyst. Analyze the following educational material and extract only the most essential information.

        Return ONLY a JSON object in this format:
        {
            "summary": "A clear 2–3 sentence summary capturing the main idea",
            "keyPoints": ["Key concept 1", "Key concept 2", "Key concept 3", "Key concept 4", "Key concept 5"]
        }

        Keep responses factual, brief, and relevant.

        Content to analyze:
        %s
        """, contentToAnalyze);
    }

    private String cleanJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new AIAnalysisException("Empty response from AI service");
        }
        return response.replaceAll("```json", "").replaceAll("```", "").trim();
    }

    private String parseSummaryResponse(String response) {
        try {
            String cleaned = cleanJsonResponse(response);
            JsonNode node = objectMapper.readTree(cleaned);
            String summary = node.path("summary").asText();
            if (summary.isEmpty()) throw new RuntimeException("Missing summary");
            return summary;
        } catch (Exception e) {
            if (enableFallbacks) {
                return "Unable to generate a summary at this time.";
            }
            throw new AIAnalysisException("Failed to parse summary response", e);
        }
    }

    private String parseAnalysisResponse(String response) {
        try {
            String cleaned = cleanJsonResponse(response);
            JsonNode node = objectMapper.readTree(cleaned);

            if (!node.has("summary") || !node.has("keyPoints")) {
                throw new RuntimeException("Response missing summary or keyPoints");
            }

            return cleaned;
        } catch (Exception e) {
            log.warn("Failed to parse simplified analysis: {}", e.getMessage());
            if (enableFallbacks) {
                try {
                    return objectMapper.writeValueAsString(createSimplifiedFallbackAnalysis());
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException("Failed to serialize fallback simplified analysis", ex);
                }
            }
            throw new AIAnalysisException("Failed to parse analysis response", e);
        }
    }

    private Map<String, Object> createSimplifiedFallbackAnalysis() {
        return Map.of(
                "summary", "The content discusses educational concepts and key ideas relevant to learning objectives.",
                "keyPoints", Arrays.asList(
                        "Identifies main themes of the content",
                        "Highlights key concepts and ideas",
                        "Emphasizes learning objectives",
                        "Encourages comprehension and application"
                )
        );
    }

    private AnalyzedContentResponse createFallbackAnalyzedResponse() {
        try {
            String fallbackJson = objectMapper.writeValueAsString(createSimplifiedFallbackAnalysis());
            return StudyMaterialMapper.mapToAnalyzedContentResponse(
                    fallbackJson, "Fallback simplified analysis generated due to AI unavailability"
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create fallback simplified analysis response", e);
        }
    }

    private String parseEnhancedQuestionResponse(String response) {
        try {
            String cleaned = cleanJsonResponse(response);
            JsonNode node = objectMapper.readTree(cleaned);

            if (!node.isArray() || node.isEmpty()) {
                throw new RuntimeException("Invalid questions format");
            }

            return cleaned;
        } catch (Exception e) {
            if (enableFallbacks) {
                return createEnhancedFallbackQuestions();
            }
            throw new AIAnalysisException("Failed to parse questions response", e);
        }
    }

    private String createEnhancedFallbackQuestions() {
        try {
            List<Map<String, Object>> fallback = List.of(
                    Map.of(
                            "type", "MULTIPLE_CHOICE",
                            "question", "Which idea is central to understanding the material?",
                            "topic", "Core Concepts",
                            "difficulty", "BEGINNER",
                            "options", Arrays.asList("Basics", "Details", "Examples", "Trivial info"),
                            "answer", "Basics",
                            "explanation", "Foundational ideas are essential for grasping the main subject."
                    )
            );
            return objectMapper.writeValueAsString(fallback);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create fallback questions", e);
        }
    }

    private boolean isQuotaError(Exception e) {
        String msg = e.getMessage();
        return msg != null && (
                msg.contains("429") ||
                        msg.contains("quota") ||
                        msg.contains("rate limit") ||
                        msg.contains("RESOURCE_EXHAUSTED")
        );
    }
}