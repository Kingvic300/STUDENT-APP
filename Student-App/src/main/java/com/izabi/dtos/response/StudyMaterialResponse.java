package com.izabi.dtos.response;

import com.izabi.data.enums.ProcessingStatus;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudyMaterialResponse {

    private String id;

    private String fileName;

    private String summary;

    private List<String> keyPoints;

    private List<StudyQuestionResponse> questions;

    private LocalDateTime createdAt;

    private String message;

    private String userId;

    private String fileContent;

    private String fileExtension;

    private int fileSize;

    private int numberOfPages;

    private boolean isActive;

    @Indexed
    private LocalDateTime uploadDate;

    private ProcessingStatus processingStatus;

    @Indexed
    private String uploadedBy;

    private LocalDateTime lastAccessedDate;

}
