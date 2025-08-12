package com.izabi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAnalysisResponse {
    private String id;
    private String fileName;
    private String summary;
    private List<String> keyTopics;
    private List<String> keyTerms;
    private String difficulty;
    private int estimatedReadingTimeMinutes;
    private String processingStatus;
    private String aiAnalysisStatus;
    private int numberOfPages;
    private LocalDateTime analyzedDate;
}
