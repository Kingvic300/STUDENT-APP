package com.izabi.service;

import com.izabi.dto.response.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

public interface AIService {
    AnalyzedContentResponse analyzeContent(String text);

    SummarizedContentResponse summarizeContent(String text);

    @Retryable(retryFor = {Exception.class}, maxAttemptsExpression = "3", backoff = @Backoff(delay = 1000, multiplier = 2))
    String generateQuestions(String text, int numberOfQuestions);
}
