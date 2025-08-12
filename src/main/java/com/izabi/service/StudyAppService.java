package com.izabi.service;

import com.izabi.dto.StudyMaterialDTO;
import org.springframework.web.multipart.MultipartFile;

public interface StudyAppService {
    StudyMaterialDTO generateStudyMaterial(MultipartFile file);
}
