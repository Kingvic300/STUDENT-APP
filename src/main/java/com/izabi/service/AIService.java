package com.izabi.service;

import com.izabi.dto.response.*;

public interface AIService {
    AnalyzedContentResponse analyzeContent(String text);

    SummarizedContentResponse summarizeContent(String text);

    String generateQuestions(String text);
}
