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
@Document(collection = "material")
public class StudyMaterial {
    @Id
    private String id;

    private String fileName;
    private String originalFileName;
    private String fileContent;
    private String fileExtension;
    private int fileSize;
    private String userId;
    private int numberOfPages;
    private String extractedText;
    private String summary;
    private List<String> keyPoints;
    private Difficulty difficulty;
    private String courseSubject;
    private String courseName;
    private ContentType contentType;
    private List<String> tags;
    private boolean isActive;

    @Indexed
    private LocalDateTime uploadDate;

    private ProcessingStatus processingStatus;

    @Indexed
    private String uploadedBy;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedDate;
}


