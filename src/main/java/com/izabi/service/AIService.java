package com.izabi.service;

import org.springframework.stereotype.Service;
import com.izabi.dto.response.*;

@Service
public interface AIService {
    AnalyzedContentResponse analyzeContent(String text);

    SummarizedContentResponse summarizeContent(String text);

    String generateQuestions(String text);
}
