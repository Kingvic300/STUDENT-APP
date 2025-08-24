package com.izabi.controller;

import com.izabi.dto.response.StudyQuestionResponse;
import com.izabi.dto.response.StudyMaterialResponse;
import com.izabi.dto.response.SummarizedContentResponse;
import com.izabi.service.StudyAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyAppService studyAppService;


    @PostMapping("/generate-questions")
    public ResponseEntity<List<StudyQuestionResponse>> generateQuestions(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "numberOfQuestions", defaultValue = "5") int numberOfQuestions
    ) {
        List<StudyQuestionResponse> questions = studyAppService.generateQuestions(file, userId, numberOfQuestions);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/summarize")
    public ResponseEntity<SummarizedContentResponse> summarizeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId
    ) {
        SummarizedContentResponse summary = studyAppService.summarizeFile(file, userId);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/generate-study-material")
    public ResponseEntity<StudyMaterialResponse> generateStudyMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "numberOfQuestions", defaultValue = "5") int numberOfQuestions
    ) {
        StudyMaterialResponse material = studyAppService.generateStudyMaterial(file, userId, numberOfQuestions);
        return ResponseEntity.ok(material);
    }

    @GetMapping("/history")
    public ResponseEntity<List<StudyMaterialResponse>> getStudyHistory(
            @RequestParam("userId") String userId
    ) {
        List<StudyMaterialResponse> history = studyAppService.getStudyHistory(userId);
        return ResponseEntity.ok(history);
    }
}
