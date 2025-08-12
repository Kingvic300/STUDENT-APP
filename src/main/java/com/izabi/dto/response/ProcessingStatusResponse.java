package com.izabi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingStatusResponse {
    private String pdfId;
    private String processingStatus;
    private String aiAnalysisStatus;
    private int progressPercentage;
    private String currentStep;
    private String estimatedTimeRemaining;
}
