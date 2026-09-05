package com.izabi.dtos.request;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
public class CreatePdfRequest {

    private String userId;

    private String fileName;

    private String extension;

    private String content;

    private int size;

    private int numberOfPages;

    @CreatedDate
    private LocalDateTime creationDate;
}
