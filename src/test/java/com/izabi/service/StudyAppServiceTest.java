//package com.izabi.service;
//
//import com.izabi.data.enums.Difficulty;
//import com.izabi.data.enums.QuestionType;
//import com.izabi.data.model.StudyMaterial;
//import com.izabi.data.model.User;
//import com.izabi.data.repository.StudyMaterialRepository;
//import com.izabi.data.repository.StudyQuestionRepository;
//import com.izabi.data.repository.UserRepository;
//import com.izabi.dto.response.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class StudyAppServiceImplTest {
//
//    @Mock
//    private FileTextExtractionService fileTextExtractionService;
//
//    @Mock
//    private AIService aiService;
//
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private QuestionGenerationService questionGenerationService;
//
//    @Mock
//    private StudyMaterialRepository studyMaterialRepository;
//
//    @Mock
//    private StudyQuestionRepository studyQuestionRepository;
//
//    @InjectMocks
//    private StudyAppServiceImpl studyAppService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void generateStudyMaterial_shouldReturnStudyMaterialRequest() {
//        // Mock input
//        MultipartFile mockFile = mock(MultipartFile.class);
//        String userId = "user123";
//        User user = new User();
//        user.setId(userId);
//
//        // FIX: stub findById instead of calling save()
//        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
//
//        when(mockFile.getOriginalFilename()).thenReturn("test.pdf");
//
//        // Mock file extraction
//        ReadDocumentResponse readResponse = new ReadDocumentResponse();
//        readResponse.setText("Extracted text");
//        when(fileTextExtractionService.navigateToProperFileExtension(mockFile)).thenReturn(readResponse);
//        when(fileTextExtractionService.getFileExtension(mockFile))
//                .thenReturn(new FileExtensionResponse("pdf", "File extension retrieved"));
//        when(fileTextExtractionService.getPageCount(mockFile))
//                .thenReturn(new PageCountResponse(5, "Page count retrieved"));
//
//        // Mock AI service
//        SummarizedContentResponse summaryResponse = new SummarizedContentResponse();
//        summaryResponse.setSummary("Summary text");
//        when(aiService.summarizeContent("Extracted text")).thenReturn(summaryResponse);
//
//        AnalyzedContentResponse analyzedResponse = new AnalyzedContentResponse();
//        analyzedResponse.setAnalyzed("Key points");
//        when(aiService.analyzeContent("Extracted text")).thenReturn(analyzedResponse);
//
//        // Mock question generation
//        StudyQuestionResponse questionResponse = new StudyQuestionResponse();
//        questionResponse.setQuestion("What is Java?");
//        questionResponse.setOptions(List.of("Lang1", "Lang2"));
//        questionResponse.setAnswer("Lang1");
//        questionResponse.setExplanation("Explanation");
//        questionResponse.setTopic("Programming");
//        questionResponse.setDifficulty(Difficulty.BEGINNER);
//        questionResponse.setQuestionType(QuestionType.MULTIPLE_CHOICE);
//
//        when(questionGenerationService.generateQuestionsFromFile(anyString(), any(), anyInt()))
//                .thenReturn(List.of(questionResponse));
//
//        // Mock repository saves
//        StudyMaterial savedMaterial = new StudyMaterial();
//        savedMaterial.setId("1L");
//        when(studyMaterialRepository.save(any(StudyMaterial.class))).thenReturn(savedMaterial);
//
//        // Execute
//        StudyMaterialResponse result = studyAppService.generateStudyMaterial(mockFile, user.getId(), anyInt());
//
//        // Assertions
//        assertNotNull(result);
//        assertEquals("Summary text", result.getSummary());
//        assertEquals(List.of("Key points"), result.getKeypoint());
//        assertEquals(1, result.getQuestions().size());
//        assertEquals("What is Java?", result.getQuestions().getFirst().getQuestion());
//
//        verify(studyQuestionRepository, times(1)).saveAll(anyList());
//    }
//}
