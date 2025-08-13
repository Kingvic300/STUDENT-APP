package com.izabi.service;

import com.izabi.data.enums.Difficulty;
import com.izabi.data.enums.QuestionType;
import com.izabi.data.model.StudyMaterial;
import com.izabi.data.model.StudyQuestion;
import com.izabi.data.repository.StudyMaterialRepository;
import com.izabi.data.repository.StudyQuestionRepository;
import com.izabi.dto.StudyMaterialDTO;
import com.izabi.dto.StudyQuestionDTO;
import com.izabi.dto.response.FileExtensionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudyAppServiceImpl implements StudyAppService {

    private final FileTextExtractionService fileTextExtractionService;
    private final OpenAIService openAIService;
    private final QuestionGenerationService questionGenerationService;

    // NEW: repositories for persistence
    private final StudyMaterialRepository studyMaterialRepository;
    private final StudyQuestionRepository studyQuestionRepository;

    @Override
    public StudyMaterialDTO generateStudyMaterial(MultipartFile file) {
        // 1) Extract text
        String extractedText = fileTextExtractionService.navigateToProperFileExtension(file);

        // 2) Summarize (and possibly analyze) with OpenAI
        Map<String, Object> summaryResult = openAIService.summarizeContent(extractedText);

        // 3) Generate questions
        List<StudyQuestionDTO> questions = questionGenerationService.generateQuestionsFromFile(
                file.getOriginalFilename(), extractedText
        );

        // 4) Persist StudyMaterial
        String fileName = file.getOriginalFilename();
        FileExtensionResponse fileExt  = fileTextExtractionService.getFileExtension(file);
        int pageCount   = fileTextExtractionService.getPageCount(file);

        // Safeguard for missing keyPoints in the summarize response
        @SuppressWarnings("unchecked")
        List<String> keyPoints = summaryResult.containsKey("keyPoints")
                && summaryResult.get("keyPoints") instanceof List
                ? (List<String>) summaryResult.get("keyPoints")
                : new ArrayList<>();

        StudyMaterial material = new StudyMaterial();
        // 🔧 Map to your actual entity fields here:
        material.setFileName(fileName);
        material.setFileExtension(fileExt);
        material.setNumberOfPages(pageCount);
        material.setSummary(String.valueOf(summaryResult.get("summary")));
        material.setKeyPoints(keyPoints);
        material.setExtractedText(extractedText); // keep full text if your entity has this field
        material.setCreatedAt(LocalDateTime.now());
        material.setActive(true);

        material = studyMaterialRepository.save(material); // get generated id

        // 5) Persist StudyQuestions (link via pdfId = materialId)
        List<StudyQuestion> toSave = new ArrayList<>();
        for (StudyQuestionDTO dto : questions) {
            StudyQuestion q = new StudyQuestion();
            // 🔧 Map to your actual entity fields here:
            q.setPdfId(material.getId()); // link
            q.setQuestion(dto.getQuestion());
            q.setOptions(dto.getOptions());
            q.setCorrectAnswer(dto.getCorrectAnswer());
            q.setDifficulty(Difficulty.valueOf(dto.getDifficulty()));      // or convert to enum if needed
            q.setQuestionType(QuestionType.valueOf(dto.getQuestionType()));  // or convert to enum if needed
            q.setActive(true);
            q.setCreatedAt(LocalDateTime.now());
            toSave.add(q);
        }
        List<StudyQuestion> saved = studyQuestionRepository.saveAll(toSave);

        // 6) Build DTO for response (include questions)
        StudyMaterialDTO studyMaterial = new StudyMaterialDTO();
        studyMaterial.setId(material.getId()); // if your DTO has id
        studyMaterial.setSummary(material.getSummary());
        studyMaterial.setKeyPoints(material.getKeyPoints());
        studyMaterial.setQuestions(questions); // already generated DTOs

        log.info("Saved StudyMaterial id={} with {} questions", material.getId(), saved.size());
        return studyMaterial;
    }
}
