package com.izabi.controller;

import com.izabi.dtos.request.GenerateQuestionsRequest;
import com.izabi.dtos.request.GenerateStudyMaterialRequest;
import com.izabi.dtos.request.GetStudyMaterialRequest;
import com.izabi.dtos.request.SummarizeContentRequest;
import com.izabi.dtos.response.StudyQuestionResponse;
import com.izabi.dtos.response.StudyMaterialResponse;
import com.izabi.dtos.response.SummarizedContentResponse;
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
    public ResponseEntity<List<StudyQuestionResponse>> generateQuestions(@ModelAttribute GenerateQuestionsRequest request) throws Exception{
        List<StudyQuestionResponse> questions = studyAppService.generateQuestions(request);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/summarize")
    public ResponseEntity<SummarizedContentResponse> summarizeFile(@ModelAttribute SummarizeContentRequest request) throws Exception{
        SummarizedContentResponse summary = studyAppService.summarizeFile(request);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/generate-study-material")
    public ResponseEntity<StudyMaterialResponse> generateStudyMaterial(@ModelAttribute GenerateStudyMaterialRequest request) throws Exception {
        StudyMaterialResponse material = studyAppService.generateStudyMaterial(request);
        return ResponseEntity.ok(material);
    }

    @GetMapping("/history")
    public ResponseEntity<List<StudyMaterialResponse>> getStudyHistory(@ModelAttribute GetStudyMaterialRequest request) throws Exception{
        List<StudyMaterialResponse> history = studyAppService.getStudyHistory(String.valueOf(request));
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/history/{studyMaterialId}")
    public ResponseEntity<Void> deleteStudyMaterial(
            @PathVariable String studyMaterialId,
            @RequestParam("userId") String userId) {

        studyAppService.deleteStudyMaterial(studyMaterialId, userId);
        return ResponseEntity.noContent().build();
    }

}
