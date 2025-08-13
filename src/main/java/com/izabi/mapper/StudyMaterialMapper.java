package com.izabi.mapper;

import com.izabi.dto.response.*;

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
}
