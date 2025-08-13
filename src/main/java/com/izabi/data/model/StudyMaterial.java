package com.izabi.data.model;

import com.izabi.data.enums.ContentType;
import com.izabi.data.enums.Difficulty;
import com.izabi.data.enums.ProcessingStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "pdfs")
public class StudyMaterial {
    @Id
    private String id;

    private String fileName;
    private String originalFileName;
    private String fileContent;
    private String fileExtension;
    private int fileSize;
    private int numberOfPages;
    private String extractedText;
    private String summary;
    private List<String> keyTopics;
    private List<String> keyTerms;
    private Difficulty difficulty;
    private int estimatedReadingTimeMinutes;
    private String courseSubject;
    private String courseName;
    private ContentType contentType;
    private List<String> tags;

    @Indexed
    private LocalDateTime uploadDate;

    private ProcessingStatus processingStatus;
    private String aiAnalysisStatus;
    private boolean isSearchable;

    @Indexed
    private String uploadedBy;
    private boolean isFavorite;
    private int viewCount;
    private LocalDateTime lastAccessedDate;
}
