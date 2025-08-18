//package com.izabi.service;
//
//import com.izabi.data.enums.Difficulty;
//import com.izabi.data.enums.QuestionType;
//import com.izabi.data.model.Question;
//import com.izabi.dto.response.ReadDocumentResponse;
//import com.izabi.dto.response.StudyQuestionResponse;
//import com.izabi.data.repository.QuestionRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("Question Generation Service Tests")
//class QuestionGenerationServiceImplTest {
//
//    @Mock
//    private AIService aiService;
//
//    @Mock
//    private FileTextExtractionService fileTextExtractionService;
//
//    @Mock
//    private QuestionRepository questionRepository;
//
//    @Mock
//    private MultipartFile multipartFile;
//
//    @InjectMocks
//    private QuestionGenerationServiceImpl questionGenerationService;
//
//    private String fileId;
//    private String extractedText;
//    private String aiJsonResponse;
//    private List<Question> sampleQuestionEntities;
//
//    @BeforeEach
//    void setUp() {
//        fileId = "test-file-123";
//        extractedText = "Sample educational content about Object-Oriented Programming";
//
//        // Sample AI JSON response
//        aiJsonResponse = """
//            [
//                {
//                    "question": "What is Object-Oriented Programming?",
//                    "questionType": "MULTIPLE_CHOICE",
//                    "options": ["A programming paradigm", "A database", "A framework", "An IDE"],
//                    "correctAnswer": "A programming paradigm",
//                    "difficulty": "BEGINNER"
//                },
//                {
//                    "question": "Name three pillars of OOP",
//                    "questionType": "SHORT_ANSWER",
//                    "options": [],
//                    "correctAnswer": "Encapsulation, Inheritance, Polymorphism",
//                    "difficulty": "INTERMEDIATE"
//                }
//            ]
//            """;
//
//        // Sample StudyQuestionResponse objects
//        List<StudyQuestionResponse> sampleStudyQuestions = Arrays.asList(
//                StudyQuestionResponse.builder()
//                        .question("What is Object-Oriented Programming?")
//                        .questionType(QuestionType.valueOf("MULTIPLE_CHOICE"))
//                        .options(Arrays.asList("A programming paradigm", "A database", "A framework", "An IDE"))
//                        .answer("A programming paradigm")
//                        .difficulty(Difficulty.valueOf("BEGINNER"))
//                        .build(),
//                StudyQuestionResponse.builder()
//                        .question("Name three pillars of OOP")
//                        .questionType(QuestionType.valueOf("SHORT_ANSWER"))
//                        .options(Collections.emptyList())
//                        .answer("Encapsulation, Inheritance, Polymorphism")
//                        .difficulty(Difficulty.valueOf("INTERMEDIATE"))
//                        .build()
//        );
//
//        // Sample Question entities
//        sampleQuestionEntities = Arrays.asList(
//                Question.builder()
//                        .id(String.valueOf(1L))
//                        .fileId(fileId)
//                        .question("What is Object-Oriented Programming?")
//                        .options(Arrays.asList("A programming paradigm", "A database", "A framework", "An IDE"))
//                        .answer("A programming paradigm")
//                        .difficulty(Difficulty.valueOf("BEGINNER"))
//                        .questionType(QuestionType.valueOf("MULTIPLE_CHOICE"))
//                        .build(),
//                Question.builder()
//                        .id(String.valueOf(2L))
//                        .fileId(fileId)
//                        .question("Name three pillars of OOP")
//                        .options(Collections.emptyList())
//                        .answer("Encapsulation, Inheritance, Polymorphism")
//                        .difficulty(Difficulty.valueOf("INTERMEDIATE"))
//                        .questionType(QuestionType.valueOf("SHORT_ANSWER"))
//                        .build()
//        );
//    }
//
//    @Test
//    @DisplayName("Should successfully generate questions from file")
//    void testGenerateQuestionsFromFile_Success() throws Exception {
//        // Arrange
//        ReadDocumentResponse readResponse = ReadDocumentResponse.builder()
//                .text(extractedText)
//                .build();
//
//        when(fileTextExtractionService.navigateToProperFileExtension(multipartFile))
//                .thenReturn(readResponse);
//        when(aiService.generateQuestions(extractedText))
//                .thenReturn(aiJsonResponse);
//        when(questionRepository.saveAll(anyList()))
//                .thenReturn(sampleQuestionEntities);
//
//        // Act
//        List<StudyQuestionResponse> result = questionGenerationService.generateQuestionsFromFile(fileId, multipartFile);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals("What is Object-Oriented Programming?", result.get(0).getQuestion());
//        assertEquals("MULTIPLE_CHOICE",String.valueOf( result.get(0).getQuestionType()));
//        assertEquals("Name three pillars of OOP", result.get(1).getQuestion());
//        assertEquals("SHORT_ANSWER",String.valueOf( result.get(1).getQuestionType()));
//
//        verify(fileTextExtractionService, times(1)).navigateToProperFileExtension(multipartFile);
//        verify(aiService, times(1)).generateQuestions(extractedText);
//        verify(questionRepository, times(1)).saveAll(anyList());
//    }
//
//    @Test
//    @DisplayName("Should throw exception when file text extraction fails")
//    void testGenerateQuestionsFromFile_FileExtractionFailure() {
//        // Arrange
//        when(fileTextExtractionService.navigateToProperFileExtension(multipartFile))
//                .thenThrow(new RuntimeException("File extraction failed"));
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(
//                RuntimeException.class,
//                () -> questionGenerationService.generateQuestionsFromFile(fileId, multipartFile)
//        );
//
//        assertEquals("Failed to generate questions", exception.getMessage());
//        verify(fileTextExtractionService, times(1)).navigateToProperFileExtension(multipartFile);
//        verify(aiService, never()).generateQuestions(anyString());
//        verify(questionRepository, never()).saveAll(anyList());
//    }
//
//    @Test
//    @DisplayName("Should throw exception when AI service fails")
//    void testGenerateQuestionsFromFile_AIServiceFailure() {
//        // Arrange
//        ReadDocumentResponse readResponse = ReadDocumentResponse.builder()
//                .text(extractedText)
//                .build();
//
//        when(fileTextExtractionService.navigateToProperFileExtension(multipartFile))
//                .thenReturn(readResponse);
//        when(aiService.generateQuestions(extractedText))
//                .thenThrow(new RuntimeException("AI service unavailable"));
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(
//                RuntimeException.class,
//                () -> questionGenerationService.generateQuestionsFromFile(fileId, multipartFile)
//        );
//
//        assertEquals("Failed to generate questions", exception.getMessage());
//        verify(fileTextExtractionService, times(1)).navigateToProperFileExtension(multipartFile);
//        verify(aiService, times(1)).generateQuestions(extractedText);
//        verify(questionRepository, never()).saveAll(anyList());
//    }
//
//    @Test
//    @DisplayName("Should throw exception when JSON parsing fails")
//    void testGenerateQuestionsFromFile_JsonParsingFailure() {
//        // Arrange
//        ReadDocumentResponse readResponse = ReadDocumentResponse.builder()
//                .text(extractedText)
//                .build();
//
//        when(fileTextExtractionService.navigateToProperFileExtension(multipartFile))
//                .thenReturn(readResponse);
//        when(aiService.generateQuestions(extractedText))
//                .thenReturn("Invalid JSON response");
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(
//                RuntimeException.class,
//                () -> questionGenerationService.generateQuestionsFromFile(fileId, multipartFile)
//        );
//
//        assertEquals("Failed to generate questions", exception.getMessage());
//        verify(fileTextExtractionService, times(1)).navigateToProperFileExtension(multipartFile);
//        verify(aiService, times(1)).generateQuestions(extractedText);
//        verify(questionRepository, never()).saveAll(anyList());
//    }
//
//    @Test
//    @DisplayName("Should throw exception when database save fails")
//    void testGenerateQuestionsFromFile_DatabaseSaveFailure() {
//        // Arrange
//        ReadDocumentResponse readResponse = ReadDocumentResponse.builder()
//                .text(extractedText)
//                .build();
//
//        when(fileTextExtractionService.navigateToProperFileExtension(multipartFile))
//                .thenReturn(readResponse);
//        when(aiService.generateQuestions(extractedText))
//                .thenReturn(aiJsonResponse);
//        when(questionRepository.saveAll(anyList()))
//                .thenThrow(new RuntimeException("Database error"));
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(
//                RuntimeException.class,
//                () -> questionGenerationService.generateQuestionsFromFile(fileId, multipartFile)
//        );
//
//        assertEquals("Failed to generate questions", exception.getMessage());
//        verify(questionRepository, times(1)).saveAll(anyList());
//    }
//
//    @Test
//    @DisplayName("Should successfully find questions by fileId")
//    void testFindQuestions_Success() {
//        // Arrange
//        when(questionRepository.findByFileId(fileId))
//                .thenReturn(sampleQuestionEntities);
//
//        // Act
//        List<StudyQuestionResponse> result = questionGenerationService.findQuestions(fileId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(2, result.size());
//
//        StudyQuestionResponse firstQuestion = result.getFirst();
//        assertEquals("1", firstQuestion.getId());
//        assertEquals("What is Object-Oriented Programming?", firstQuestion.getQuestion());
//        assertEquals("MULTIPLE_CHOICE", String.valueOf(firstQuestion.getQuestionType()));
//        assertEquals("A programming paradigm", firstQuestion.getAnswer());
//        assertEquals("BEGINNER", String.valueOf(firstQuestion.getDifficulty()));
//        assertNull(firstQuestion.getExplanation());
//        assertNull(firstQuestion.getTopic());
//        assertNotNull(firstQuestion.getCreatedAt());
//
//        verify(questionRepository, times(1)).findByFileId(fileId);
//    }
//
//    @Test
//    @DisplayName("Should return empty list when no questions found")
//    void testFindQuestions_NoQuestionsFound() {
//        // Arrange
//        when(questionRepository.findByFileId(fileId))
//                .thenReturn(Collections.emptyList());
//
//        // Act
//        List<StudyQuestionResponse> result = questionGenerationService.findQuestions(fileId);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//        verify(questionRepository, times(1)).findByFileId(fileId);
//    }
//
//    @Test
//    @DisplayName("Should successfully delete questions when they exist")
//    void testDeleteQuestions_Success() {
//        // Arrange
//        when(questionRepository.findByFileId(fileId))
//                .thenReturn(sampleQuestionEntities);
//        doNothing().when(questionRepository).deleteByFileId(fileId);
//
//        // Act
//        boolean result = questionGenerationService.deleteQuestions(fileId);
//
//        // Assert
//        assertTrue(result);
//        verify(questionRepository, times(1)).findByFileId(fileId);
//        verify(questionRepository, times(1)).deleteByFileId(fileId);
//    }
//
//    @Test
//    @DisplayName("Should return false when no questions exist to delete")
//    void testDeleteQuestions_NoQuestionsExist() {
//        // Arrange
//        when(questionRepository.findByFileId(fileId))
//                .thenReturn(Collections.emptyList());
//
//        // Act
//        boolean result = questionGenerationService.deleteQuestions(fileId);
//
//        // Assert
//        assertFalse(result);
//        verify(questionRepository, times(1)).findByFileId(fileId);
//        verify(questionRepository, never()).deleteByFileId(fileId);
//    }
//
//    @Test
//    @DisplayName("Should handle null fileId gracefully in findQuestions")
//    void testFindQuestions_NullFileId() {
//        // Arrange
//        when(questionRepository.findByFileId(null))
//                .thenReturn(Collections.emptyList());
//
//        // Act
//        List<StudyQuestionResponse> result = questionGenerationService.findQuestions(null);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//        verify(questionRepository, times(1)).findByFileId(null);
//    }
//
//    @Test
//    @DisplayName("Should handle null fileId gracefully in deleteQuestions")
//    void testDeleteQuestions_NullFileId() {
//        // Arrange
//        when(questionRepository.findByFileId(null))
//                .thenReturn(Collections.emptyList());
//
//        // Act
//        boolean result = questionGenerationService.deleteQuestions(null);
//
//        // Assert
//        assertFalse(result);
//        verify(questionRepository, times(1)).findByFileId(null);
//        verify(questionRepository, never()).deleteByFileId(null);
//    }
//
//    @Test
//    @DisplayName("Should verify entity mapping from StudyQuestionResponse to Question")
//    void testEntityMapping() throws Exception {
//        // Arrange
//        ReadDocumentResponse readResponse = ReadDocumentResponse.builder()
//                .text(extractedText)
//                .build();
//
//        when(fileTextExtractionService.navigateToProperFileExtension(multipartFile))
//                .thenReturn(readResponse);
//        when(aiService.generateQuestions(extractedText))
//                .thenReturn(aiJsonResponse);
//        when(questionRepository.saveAll(anyList()))
//                .thenReturn(sampleQuestionEntities);
//
//        // Act
//        questionGenerationService.generateQuestionsFromFile(fileId, multipartFile);
//
//        // Assert - Verify that saveAll was called with correct Question entities
//        verify(questionRepository).saveAll(argThat(questions -> {
//            List<Question> questionList = (List<Question>) questions;
//            return questionList.size() == 2 &&
//                    questionList.get(0).getFileId().equals(fileId) &&
//                    questionList.get(0).getQuestion().equals("What is Object-Oriented Programming?") &&
//                    questionList.get(1).getQuestion().equals("Name three pillars of OOP");
//        }));
//    }
//
//    @Test
//    @DisplayName("Should handle empty AI response")
//    void testGenerateQuestionsFromFile_EmptyAIResponse() {
//        // Arrange
//        ReadDocumentResponse readResponse = ReadDocumentResponse.builder()
//                .text(extractedText)
//                .build();
//
//        when(fileTextExtractionService.navigateToProperFileExtension(multipartFile))
//                .thenReturn(readResponse);
//        when(aiService.generateQuestions(extractedText))
//                .thenReturn("[]");
//        when(questionRepository.saveAll(anyList()))
//                .thenReturn(Collections.emptyList());
//
//        // Act
//        List<StudyQuestionResponse> result = questionGenerationService.generateQuestionsFromFile(fileId, multipartFile);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//        verify(questionRepository, times(1)).saveAll(Collections.emptyList());
//    }
//}