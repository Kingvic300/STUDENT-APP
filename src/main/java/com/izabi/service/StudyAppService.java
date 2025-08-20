package com.izabi.service;

import com.izabi.dto.response.StudyMaterialResponse;
import org.springframework.web.multipart.MultipartFile;

public interface StudyAppService {
    StudyMaterialResponse generateStudyMaterial(MultipartFile file);
}
