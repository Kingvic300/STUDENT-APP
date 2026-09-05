package com.izabi.data.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("pdfs")
public class Pdf {

    @Id
    private String id;

    private String userId;

    private String fileName;

    private String extension;

    private String content;

    private int size;

    private int numberOfPages;

    @CreatedDate
    private LocalDateTime creationDate;
}
