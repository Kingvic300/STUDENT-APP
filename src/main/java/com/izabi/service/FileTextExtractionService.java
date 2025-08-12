package com.izabi.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileTextExtractionService {

    String getFileExtension(MultipartFile file);

    String navigateToProperFileExtension(MultipartFile file);

    int getPageCount(MultipartFile file);

}
