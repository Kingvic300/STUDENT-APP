package com.izabi.service;

import com.izabi.dto.response.*;
import com.izabi.mapper.StudyMaterialMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import com.izabi.exception.*;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class FileTextExtractionServiceImpl implements FileTextExtractionService {

    @Override
    public FileExtensionResponse getFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String extension;

        if (filename == null) {
            throw new NoFileFoundException("File cannot be null");
        }

        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1) {
            throw new NoFileExtensionFoundException("No file extension found");
        } else {
            extension = filename.substring(lastIndex + 1).toLowerCase();
        }
        return StudyMaterialMapper.mapToFileExtensionResponse(extension, "file extension found ");
    }

    @Override
    public ReadDocumentResponse navigateToProperFileExtension(MultipartFile file) {
        FileExtensionResponse fileExtension = getFileExtension(file);
        String extension = fileExtension.getFileExtension();

        return switch (extension) {
            case "txt", "csv" -> readTextFile(file);
            case "pdf" -> readPDF(file);
            case "docx", "doc" -> readWordDocument(file);
            case "xlsx", "xls" -> readExcelFile(file);
            default -> throw new FileExtensionNotSupportedException("Unsupported file extension: " + extension);
        };
    }

    @Override
    public PageCountResponse getPageCount(MultipartFile file) {
        FileExtensionResponse fileExtension = getFileExtension(file);
        String extension = fileExtension.getFileExtension();
        try {
            return switch (extension) {
                case "txt", "csv" -> getTextFilePageCount(file);
                case "pdf" -> getPDFPageCount(file);
                case "docx", "doc" -> getWordPageCount(file);
                case "xlsx", "xls" -> getExcelSheetCount(file);
                default -> throw new FileExtensionNotSupportedException("Unsupported file extension: " + extension);
            };
        } catch (Exception e) {
            throw new RuntimeException("Error counting pages: " + e.getMessage(), e);
        }
    }

    private PageCountResponse getExcelSheetCount(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            return StudyMaterialMapper.mapToPageCountResponse(workbook.getNumberOfSheets(),"Page was counted Successfully");
        } catch (Exception e) {
            throw new FileExtensionNotSupportedException("FILE NOT SUPPORTED: " + e.getMessage());
        }
    }

    private PageCountResponse getWordPageCount(MultipartFile file) {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            return StudyMaterialMapper.mapToPageCountResponse(document.getParagraphs().size(),"Page was counted Successfully");
        } catch (Exception e) {
            throw new FileExtensionNotSupportedException("FILE NOT SUPPORTED: " + e.getMessage());
        }
    }

    private PageCountResponse getPDFPageCount(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            return StudyMaterialMapper.mapToPageCountResponse(document.getNumberOfPages(),"Page was counted Successfully");
        } catch (Exception e) {
            throw new FileExtensionNotSupportedException("FILE NOT SUPPORTED: " + e.getMessage());
        }
    }

    private PageCountResponse getTextFilePageCount(MultipartFile file) throws Exception {
        int linesPerPage = 50;
        double lines = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            while (reader.readLine() != null) {
                lines++;
            }
        }
        double division = lines /linesPerPage;
        int result = (int) Math.ceil(division);
        return StudyMaterialMapper.mapToPageCountResponse(result,"Page was counted Successfully");
    }

    private ReadDocumentResponse readExcelFile(MultipartFile file) {
        StringBuilder content = new StringBuilder();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        content.append(formatter.formatCellValue(cell)).append("\t");
                    }
                    content.append("\n");
                }
            }
        } catch (Exception e) {
            log.error("Error reading Excel file: {}", e.getMessage(), e);
            throw new DocumentNotReadException("Error while reading study material");
        }

        return StudyMaterialMapper.mapToReadDocumentResponse(
                content.toString(),
                "Excel file was read successfully"
        );
    }

    private ReadDocumentResponse readWordDocument(MultipartFile file) {
        StringBuilder content = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            document.getParagraphs().forEach(p -> content.append(p.getText()).append("\n"));
        } catch (Exception e) {
            log.error("Error reading Word document: {}", e.getMessage(), e);
            throw new DocumentNotReadException("Error while reading study material");
        }
        return StudyMaterialMapper.mapToReadDocumentResponse(content.toString(), "Doc was read successfully");
    }

    private ReadDocumentResponse readPDF(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return StudyMaterialMapper.mapToReadDocumentResponse(stripper.getText(document), "PDF was read successfully");
        } catch (Exception e) {
            log.error("Error reading PDF file: {}", e.getMessage(), e);
            throw new DocumentNotReadException("Error while reading study material");
        }
    }

    private ReadDocumentResponse readTextFile(MultipartFile file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            log.error("Error reading text file: {}", e.getMessage(), e);
            throw new DocumentNotReadException("Error while reading study material");

        }
        return StudyMaterialMapper.mapToReadDocumentResponse(content.toString(), "text file was read successfully");
    }
}
