package com.izabi.data.model;

import com.izabi.data.enums.ContentType;
import com.izabi.data.enums.Difficulty;
import com.izabi.data.enums.ProcessingStatus;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
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

    private String userId;

    private String fileName;

    private String fileContent;

    private String fileExtension;

    private int fileSize;

    private int numberOfPages;

    private String summary;

    private List<String> keyPoints;

    private boolean isActive;

    @Indexed
    private LocalDateTime uploadDate;

    private ProcessingStatus processingStatus;

    @Indexed
    private String uploadedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime lastAccessedDate;
}


