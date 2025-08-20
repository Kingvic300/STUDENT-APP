package com.izabi.mapper;

import com.izabi.data.model.StudyMaterial;
import com.izabi.data.model.StudyQuestion;
import com.izabi.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public class StudyMaterialMapper {
    public static FileExtensionResponse mapToFileExtensionResponse(String extension, String message){
        FileExtensionResponse response = new FileExtensionResponse();
        response.setFileExtension(extension);
        response.setMessage(message);
        return response;
    }
    public static ReadDocumentResponse mapToReadDocumentResponse(String text, String message){
        ReadDocumentResponse response = new ReadDocumentResponse();
        response.setText(text);
        response.setMessage(message);
        return response;
    }
    public static PageCountResponse mapToPageCountResponse(int numberOfPages, String message){
        PageCountResponse response = new PageCountResponse();
        response.setNumberOfPages(numberOfPages);
        response.setMessage(message);
        return response;
    }
    public static SummarizedContentResponse mapToSummarizedContentResponse(String summary, String message){
        SummarizedContentResponse response = new SummarizedContentResponse();
        response.setSummary(summary);
        response.setMessage(message);
        return response;
    }
    public static AnalyzedContentResponse mapToAnalyzedContentResponse(String analyzed, String message){
        AnalyzedContentResponse response = new AnalyzedContentResponse();
        response.setAnalyzed(analyzed);
        response.setMessage(message);
        return response;
    }
    public static StudyMaterialResponse mapToStudyMaterialResponse(
            String summary, List<String> keypoint, List<StudyQuestion> questions, String message){
        StudyMaterialResponse studyMaterialResponse = new StudyMaterialResponse();
        studyMaterialResponse.setKeypoint(keypoint);
        studyMaterialResponse.setQuestions(questions);
        studyMaterialResponse.setSummary(summary);
        studyMaterialResponse.setMessage(message);
        return studyMaterialResponse;
    }
    public static StudyMaterial mapToStudyMaterial(
            MultipartFile file,
            FileExtensionResponse fileExt,
            PageCountResponse pageCount,
            ReadDocumentResponse extracted,
            SummarizedContentResponse summaryResponse,
            AnalyzedContentResponse analysisResponse
            ){
        StudyMaterial studyMaterial = new StudyMaterial();
        studyMaterial.setFileName(file.getOriginalFilename());
        studyMaterial.setFileExtension(fileExt.getFileExtension());
        studyMaterial.setNumberOfPages(pageCount.getNumberOfPages());
        studyMaterial.setExtractedText(extracted.getText());
        studyMaterial.setSummary(summaryResponse.getSummary());
        studyMaterial.setKeyPoints(List.of(analysisResponse.getAnalyzed()));
        studyMaterial.setActive(true);
        studyMaterial.setUploadDate(LocalDateTime.now());
        return studyMaterial;
    }
}
