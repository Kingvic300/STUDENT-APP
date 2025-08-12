package com.izabi.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface OpenAIService {
    Map<String, Object> analyzeContent(String text);

    Map<String, Object> summarizeContent(String text);
}
