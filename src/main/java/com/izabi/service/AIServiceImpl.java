package com.izabi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izabi.dto.response.AnalyzedContentResponse;
import com.izabi.dto.response.SummarizedContentResponse;
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
    @Retryable(retryFor = {Exception.class},maxAttemptsExpression = "3", backoff = @Backoff(delay = 1000, multiplier = 2))
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

    private String createEnhancedQuestionGenerationPrompt(String text, int numberOfQuestions) {
        String contentToUse = truncateContent(text);

        return "You are an expert educator. Generate " + numberOfQuestions + " objective MULTIPLE_CHOICE questions based on the following educational content.\n"
                + "Each question MUST include exactly 4 options, one correct answer, and a detailed explanation.\n\n"
                + "IMPORTANT: Return ONLY a JSON array with this exact structure (no additional text, no markdown formatting):\n"
                + "[\n"
                + "    {\n"
                + "        \"type\": \"MULTIPLE_CHOICE\",\n"
                + "        \"question\": \"The study question text\",\n"
                + "        \"topic\": \"The main topic this question covers\",\n"
                + "        \"difficulty\": \"BEGINNER or INTERMEDIATE or ADVANCED\",\n"
                + "        \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n"
                + "        \"answer\": \"The correct option (must match one of the options exactly)\",\n"
                + "        \"explanation\": \"Detailed explanation of why this is the correct answer and why the others are incorrect\"\n"
                + "    }\n"
                + "]\n\n"
                + "Guidelines:\n"
                + "- ALL questions must be MULTIPLE_CHOICE (no theory or open-ended questions)\n"
                + "- Generate exactly " + numberOfQuestions + " questions\n"
                + "- Each question MUST have exactly 4 plausible options\n"
                + "- Difficulty should match the complexity of the concept being tested\n"
                + "- Explanations should be educational and comprehensive\n\n"
                + "Content:\n"
                + contentToUse;
    }

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

            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new RuntimeException("Gemini API rate limit exceeded", e);
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new RuntimeException("Gemini API access denied - check API key and permissions", e);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new RuntimeException("Invalid request to Gemini API", e);
            }

            throw new RuntimeException("Gemini API call failed: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
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
                        Map.of(
                                "category", "HARM_CATEGORY_HARASSMENT",
                                "threshold", "BLOCK_MEDIUM_AND_ABOVE"
                        ),
                        Map.of(
                                "category", "HARM_CATEGORY_HATE_SPEECH",
                                "threshold", "BLOCK_MEDIUM_AND_ABOVE"
                        ),
                        Map.of(
                                "category", "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                                "threshold", "BLOCK_MEDIUM_AND_ABOVE"
                        ),
                        Map.of(
                                "category", "HARM_CATEGORY_DANGEROUS_CONTENT",
                                "threshold", "BLOCK_MEDIUM_AND_ABOVE"
                        )
                )
        );

        return new HttpEntity<>(requestBody, headers);
    }

    private String extractGeminiResponse(String response){
        try {
            JsonNode responseNode = objectMapper.readTree(response);

            if (responseNode.has("error")) {
                JsonNode error = responseNode.get("error");
                String errorMessage = error.path("message").asText();
                String errorCode = error.path("code").asText();
                throw new RuntimeException("Gemini API error (" + errorCode + "): " + errorMessage);
            }

            JsonNode candidates = responseNode.path("candidates");

            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && !parts.isEmpty()) {
                    String text = parts.get(0).path("text").asText();

                    String finishReason = firstCandidate.path("finishReason").asText();
                    if ("MAX_TOKENS".equals(finishReason)) {
                        log.warn("Gemini response was truncated due to length limit");
                    } else if ("SAFETY".equals(finishReason)) {
                        throw new RuntimeException("Content was blocked by Gemini safety filters");
                    } else if ("RECITATION".equals(finishReason)) {
                        log.warn("Gemini detected potential copyright content");
                    }

                    if (text != null && !text.trim().isEmpty()) {
                        return text.trim();
                    }
                }
            }

            throw new RuntimeException("No valid response from Gemini API");
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini response: {}", response);
            throw new RuntimeException("Failed to parse Gemini API response", e);
        }
    }

    private String truncateContent(String text) {
        if (text.length() <= maxContentLength) {
            return text;
        }

        log.info("Truncating content from {} to {} characters", text.length(), maxContentLength);
        return text.substring(0, maxContentLength) + "...";
    }

    private String createSummaryPrompt(String text) {
        String contentToSummarize = truncateContent(text);
        return String.format("""
            You are an expert educational content summarizer. Summarize the following educational content in 3–5 concise sentences.
            Focus on the main concepts, key learning objectives, and important details.
            
            IMPORTANT: Return ONLY a JSON object with this exact structure (no additional text, no markdown formatting):
            {
                "summary": "The concise summary of the content"
            }

            Content to summarize:
            %s
            """, contentToSummarize);
    }



    private String createAnalysisPrompt(String text) {
        String contentToAnalyze = truncateContent(text);
        return String.format("""
        You are an expert educational content analyst. Analyze this educational content thoroughly.
        Identify the main subject, difficulty level, key concepts, and important terms.

        IMPORTANT: Return ONLY a JSON object with this exact structure (no additional text, no markdown formatting):
        {
            "summary": "A concise 2-3 paragraph summary of the main concepts",
            "keyTopics": ["topic1", "topic2", "topic3"],
            "keyTerms": ["term1", "term2", "term3", "term4", "term5"],
            "difficulty": "BEGINNER or INTERMEDIATE or ADVANCED",
            "estimatedReadingTimeMinutes": 15,
            "courseSubject": "The main subject area of the content",
            "contentType": "LECTURE_NOTES or TEXTBOOK or ASSIGNMENT or REFERENCE",
            "questions": [
                {
                    "id": 1,
                    "type": "MULTIPLE_CHOICE",
                    "question": "Question text here",
                    "options": ["option A", "option B", "option C", "option D"],
                    "correctOptionIndex": 0,
                    "explanation": "One-sentence explanation why the correct option is correct"
                }
                /* Repeat for each question */
            ]
        }

        Rules for the QUESTIONS array (STRICT - must be followed):
        1. Produce ONLY MULTIPLE_CHOICE questions (no short answer, no true/false). Set "type" to "MULTIPLE_CHOICE".
        2. Generate exactly 5 questions unless otherwise instructed. Each question MUST have exactly 4 options.
        3. "correctOptionIndex" MUST be a zero-based integer (0..3) that points to the correct option.
        4. Questions should cover different keyTopics returned above where possible.
        5. Distractors (incorrect options) must be plausible, non-contradictory, and similar in length/complexity to the correct option.
        6. Provide a brief 1-2 sentence "explanation" that justifies the correct option (useful for feedback).
        7. Match question difficulty to the inferred "difficulty" field (BEGINNER, INTERMEDIATE, or ADVANCED).
        8. Ensure the final output is VALID JSON (no surrounding text, no Markdown, no extra comments, no trailing commas).
        9. If the content is insufficient to generate 5 high-quality questions, generate as many as you can (minimum 1) and set an additional field "generatedQuestionCount": <number> at the same level as "questions".

        Provide accurate, evidence-based items derived from the content.

        Content to analyze:
        %s
        """, contentToAnalyze);
    }


    private String cleanJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new AIAnalysisException("Empty response from AI service");
        }

        String cleaned = response.replaceAll("```json", "").replaceAll("```", "").trim();

        if (cleaned.length() > 200) {
            log.debug("Cleaned AI response: {}...", cleaned.substring(0, 200));
        } else {
            log.debug("Cleaned AI response: {}", cleaned);
        }

        return cleaned;
    }

    private String parseSummaryResponse(String response) {
        try {
            String cleanedResponse = cleanJsonResponse(response);
            JsonNode contentNode = objectMapper.readTree(cleanedResponse);
            String summary = contentNode.path("summary").asText();

            if (summary.isEmpty()) {
                throw new RuntimeException("No summary found in response");
            }

            if (summary.length() < 10) {
                log.warn("Summary appears too short: {}", summary);
            }

            return summary;

        } catch (Exception e) {
            log.warn("Failed to parse AI summary response: {}", e.getMessage());
            if (enableFallbacks) {
                return "Unable to generate a detailed summary at this time. The content contains educational material that may require manual review.";
            }
            throw new AIAnalysisException("Failed to parse summary response", e);
        }
    }

    private String parseAnalysisResponse(String response) {
        try {
            String cleanedResponse = cleanJsonResponse(response);

            JsonNode testNode = objectMapper.readTree(cleanedResponse);
            validateAnalysisFields(testNode);

            return cleanedResponse;

        } catch (JsonProcessingException e) {
            log.warn("Failed to parse AI analysis response (JSON error): {}", e.getMessage());
            if (enableFallbacks) {
                try {
                    return objectMapper.writeValueAsString(createFallbackAnalysis());
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException("Failed to serialize fallback analysis", ex);
                }
            }
            throw new AIAnalysisException("Failed to parse analysis response", e);
        } catch (Exception e) {
            log.warn("Unexpected error parsing AI analysis response: {}", e.getMessage());

            if (enableFallbacks) {
                try {
                    return objectMapper.writeValueAsString(createFallbackAnalysis());
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException("Failed to serialize fallback analysis", ex);
                }
            }
            throw new AIAnalysisException("Failed to parse analysis response", e);
        }
    }

    private String parseEnhancedQuestionResponse(String response) {
        try {
            String cleanedResponse = cleanJsonResponse(response);
            JsonNode questionsNode = objectMapper.readTree(cleanedResponse);

            if (!questionsNode.isArray() || questionsNode.isEmpty()) {
                throw new RuntimeException("Invalid questions format - not an array or empty");
            }

            for (JsonNode questionNode : questionsNode) {
                validateEnhancedQuestionStructure(questionNode);
            }

            log.info("Successfully parsed {} enhanced questions", questionsNode.size());
            return cleanedResponse;

        } catch (Exception e) {
            log.warn("Failed to parse enhanced questions response: {}", e.getMessage());
            if (enableFallbacks) {
                return createEnhancedFallbackQuestions();
            }
            throw new AIAnalysisException("Failed to parse enhanced questions response", e);
        }
    }

    private void validateAnalysisFields(JsonNode node) {
        String[] requiredFields = {"summary", "keyTopics", "keyTerms", "difficulty", "estimatedReadingTimeMinutes", "courseSubject", "contentType"};

        for (String field : requiredFields) {
            if (!node.has(field)) {
                throw new RuntimeException("Missing required field in analysis response: " + field);
            }
        }

        String difficulty = node.path("difficulty").asText();
        if (!Arrays.asList("BEGINNER", "INTERMEDIATE", "ADVANCED").contains(difficulty)) {
            log.warn("Invalid difficulty level: {}", difficulty);
        }
    }

    private void validateEnhancedQuestionStructure(JsonNode questionNode) {
        String[] requiredFields = {"type", "question", "topic", "difficulty", "explanation"};

        for (String field : requiredFields) {
            if (!questionNode.has(field)) {
                throw new RuntimeException("Missing required field in question: " + field);
            }
        }

        String questionType = questionNode.path("type").asText();
        if (!Arrays.asList("MULTIPLE_CHOICE", "THEORY").contains(questionType)) {
            throw new RuntimeException("Invalid question type: " + questionType);
        }

        String difficulty = questionNode.path("difficulty").asText();
        if (!Arrays.asList("BEGINNER", "INTERMEDIATE", "ADVANCED").contains(difficulty)) {
            log.warn("Invalid difficulty level in question: {}", difficulty);
        }

        if ("MULTIPLE_CHOICE".equals(questionType)) {
            if (!questionNode.has("options") || !questionNode.has("answer")) {
                throw new RuntimeException("MULTIPLE_CHOICE question missing options or answer");
            }

            JsonNode optionsNode = questionNode.path("options");
            if (!optionsNode.isArray() || optionsNode.size() != 4) {
                throw new RuntimeException("MULTIPLE_CHOICE question must have exactly 4 options");
            }

            String correctAnswer = questionNode.path("answer").asText();
            boolean answerFound = false;
            for (JsonNode option : optionsNode) {
                if (option.asText().equals(correctAnswer)) {
                    answerFound = true;
                    break;
                }
            }

            if (!answerFound) {
                log.warn("Correct answer '{}' not found in options for question", correctAnswer);
            }
        }
    }

    private AnalyzedContentResponse createFallbackAnalyzedResponse() {
        try {
            String fallbackJson = objectMapper.writeValueAsString(createFallbackAnalysis());
            return StudyMaterialMapper.mapToAnalyzedContentResponse(fallbackJson, "Fallback analysis generated due to AI service unavailability");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create fallback analysis response", e);
        }
    }

    private Map<String, Object> createFallbackAnalysis() {
        return Map.of(
                "summary", "Educational content covering various concepts relevant to the subject matter. Manual review recommended for detailed analysis.",
                "keyTopics", Arrays.asList("General Concepts", "Core Principles", "Applied Knowledge"),
                "keyTerms", Arrays.asList("Concept", "Theory", "Application", "Practice", "Understanding"),
                "difficulty", "INTERMEDIATE",
                "estimatedReadingTimeMinutes", 15,
                "courseSubject", "General Education",
                "contentType", "REFERENCE"
        );
    }

    private String createEnhancedFallbackQuestions() {
        try {
            List<Map<String, Object>> fallbackQuestions = Arrays.asList(
                    Map.of(
                            "type", "MULTIPLE_CHOICE",
                            "question", "Based on the content provided, which concept is most fundamental to understanding the subject matter?",
                            "topic", "Core Concepts",
                            "difficulty", "BEGINNER",
                            "options", Arrays.asList("Basic principles", "Advanced applications", "Historical context", "Future implications"),
                            "answer", "Basic principles",
                            "explanation", "Basic principles form the foundation of any subject matter and must be understood before progressing to more complex topics. Advanced applications build upon these principles, while historical context and future implications provide additional perspective but are not fundamental to core understanding."
                    ),
                    Map.of(
                            "type", "THEORY",
                            "question", "Explain the importance of understanding fundamental concepts before moving to advanced applications in this subject area.",
                            "topic", "Learning Methodology",
                            "difficulty", "INTERMEDIATE",
                            "explanation", "A good response should discuss how fundamental concepts provide the building blocks for advanced understanding, the risks of skipping foundational knowledge, and how proper sequencing of learning leads to better comprehension and retention. Students should also mention practical examples of how basic concepts apply to complex scenarios."
                    ),
                    Map.of(
                            "type", "MULTIPLE_CHOICE",
                            "question", "What approach would be most effective for studying this material?",
                            "topic", "Study Strategies",
                            "difficulty", "BEGINNER",
                            "options", Arrays.asList("Memorization only", "Understanding concepts and applications", "Skipping difficult sections", "Reading once quickly"),
                            "answer", "Understanding concepts and applications",
                            "explanation", "Understanding concepts and applications is the most effective approach as it promotes deep learning and retention. Memorization only leads to superficial knowledge, skipping difficult sections creates knowledge gaps, and reading once quickly doesn't allow for proper comprehension and integration of information."
                    )
            );

            return objectMapper.writeValueAsString(fallbackQuestions);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create enhanced fallback questions", e);
        }
    }
    private boolean isQuotaError(Exception e) {
        String message = e.getMessage();
        return message != null && (
                message.contains("429") ||
                        message.contains("quota") ||
                        message.contains("insufficient_quota") ||
                        message.contains("rate limit") ||
                        message.contains("RESOURCE_EXHAUSTED") ||
                        message.contains("quota exceeded")
        );
    }
}