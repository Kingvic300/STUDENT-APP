package com.izabi.service;

import com.izabi.exception.FileExtensionNotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
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
    public String getFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return "";

        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1) {
            return "";
        } else {
            return filename.substring(lastIndex + 1).toLowerCase();
        }
    }

    @Override
    public String navigateToProperFileExtension(MultipartFile file) {
        String extension = getFileExtension(file);

        return switch (extension) {
            case "txt", "csv" -> readTextFile(file);
            case "pdf" -> readPDF(file);
            case "docx", "doc" -> readWordDocument(file);
            case "xlsx", "xls" -> readExcelFile(file);
            default -> throw new FileExtensionNotSupportedException("Unsupported file extension: " + extension);
        };
    }

    @Override
    public int getPageCount(MultipartFile file) {
        String extension = getFileExtension(file);

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

    private int getExcelSheetCount(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            return workbook.getNumberOfSheets();
        } catch (Exception e) {
            throw new FileExtensionNotSupportedException("FILE NOT SUPPORTED: " + e.getMessage());
        }
    }

    private int getWordPageCount(MultipartFile file) {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            return document.getParagraphs().size();
        } catch (Exception e) {
            throw new FileExtensionNotSupportedException("FILE NOT SUPPORTED: " + e.getMessage());
        }
    }

    private int getPDFPageCount(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            return document.getNumberOfPages();
        } catch (Exception e) {
            throw new FileExtensionNotSupportedException("FILE NOT SUPPORTED: " + e.getMessage());
        }
    }

    private int getTextFilePageCount(MultipartFile file) throws Exception {
        int linesPerPage = 50;
        double lines = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            while (reader.readLine() != null) {
                lines++;
            }
        }
        double division = lines /linesPerPage;
        return (int) Math.ceil(division);
    }

    private String readExcelFile(MultipartFile file) {
        StringBuilder content = new StringBuilder();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        content.append(cell.toString()).append("\t");
                    }
                    content.append("\n");
                }
            }
        } catch (Exception e) {
            log.error("Error reading Excel file: {}", e.getMessage(), e);
        }
        return content.toString();
    }

    private String readWordDocument(MultipartFile file) {
        StringBuilder content = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            document.getParagraphs().forEach(p -> content.append(p.getText()).append("\n"));
        } catch (Exception e) {
            log.error("Error reading Word document: {}", e.getMessage(), e);
        }
        return content.toString();
    }

    private String readPDF(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            log.error("Error reading PDF file: {}", e.getMessage(), e);
            return "";
        }
    }

    private String readTextFile(MultipartFile file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            log.error("Error reading text file: {}", e.getMessage(), e);
        }
        return content.toString();
    }
}
