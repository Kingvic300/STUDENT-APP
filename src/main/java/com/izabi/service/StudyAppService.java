package com.izabi.service;

import com.izabi.dto.request.StudyMaterialRequest;
import org.springframework.web.multipart.MultipartFile;

public interface StudyAppService {
    StudyMaterialRequest generateStudyMaterial(MultipartFile file);
}
