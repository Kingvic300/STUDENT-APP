package com.izabi.dtos.request;

import com.izabi.dtos.response.ReadDocumentResponse;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SaveFileToDatabaseRequest {

    private String userId;

    private MultipartFile file;

    private ReadDocumentResponse response;

    private int numberOfPages;
}
