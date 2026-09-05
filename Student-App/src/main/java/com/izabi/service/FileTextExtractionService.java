package com.izabi.service;

import com.izabi.dtos.request.NavigateToProperFileExtensionRequest;
import com.izabi.dtos.response.*;
import org.springframework.web.multipart.MultipartFile;

public interface FileTextExtractionService {

    FileExtensionResponse getFileExtension(MultipartFile file);

    ReadDocumentResponse navigateToProperFileExtension(NavigateToProperFileExtensionRequest request) throws Exception;

    PageCountResponse getPageCount(MultipartFile file);

}
