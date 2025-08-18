package com.izabi.controller;

import com.izabi.dto.request.StudyMaterialRequest;
import com.izabi.service.StudyAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/study")
@Slf4j
@RequiredArgsConstructor
public class StudyController {

    private final StudyAppService studyAppService;

    @PostMapping("/generate")
    public ResponseEntity<StudyMaterialRequest> generateStudyMaterial(@RequestParam("file") MultipartFile file) {
        log.info("Received file for study material generation: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            log.warn("Uploaded file is empty.");
            return ResponseEntity.badRequest().build();
        }

        try {
            StudyMaterialRequest studyMaterial = studyAppService.generateStudyMaterial(file);
            return ResponseEntity.ok(studyMaterial);
        } catch (Exception e) {
            log.error("Error generating study material for file {}: {}", file.getOriginalFilename(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
