package com.izabi.mapper;

import com.izabi.data.model.Pdf;
import com.izabi.dtos.request.CreatePdfRequest;

import java.time.LocalDateTime;
import java.util.UUID;

public class PdfMapper {

    public static Pdf mapToPdf(CreatePdfRequest createPdfRequest) {
        Pdf pdf = new Pdf();
        pdf.setId(UUID.randomUUID().toString());
        pdf.setUserId(createPdfRequest.getUserId());
        pdf.setContent(createPdfRequest.getContent());
        pdf.setSize(createPdfRequest.getSize());
        pdf.setFileName(createPdfRequest.getFileName());
        pdf.setExtension(createPdfRequest.getExtension());
        pdf.setNumberOfPages(createPdfRequest.getNumberOfPages());
        pdf.setCreationDate(LocalDateTime.now());
        return pdf;

    }
}
