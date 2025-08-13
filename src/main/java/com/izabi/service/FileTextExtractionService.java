package com.izabi.service;

import com.izabi.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

public interface FileTextExtractionService {

    FileExtensionResponse getFileExtension(MultipartFile file);

    ReadDocumentResponse navigateToProperFileExtension(MultipartFile file);

    PageCountResponse getPageCount(MultipartFile file);

}
