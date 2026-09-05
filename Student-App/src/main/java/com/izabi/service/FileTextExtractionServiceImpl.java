package com.izabi.service;

import com.izabi.data.model.Pdf;
import com.izabi.data.model.User;
import com.izabi.data.repository.PdfRepository;
import com.izabi.data.repository.UserRepository;
import com.izabi.dtos.request.CreatePdfRequest;
import com.izabi.dtos.request.NavigateToProperFileExtensionRequest;
import com.izabi.dtos.response.*;
import com.izabi.exception.*;
import com.izabi.mapper.PdfMapper;
import com.izabi.mapper.StudyMaterialMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileTextExtractionServiceImpl implements FileTextExtractionService {

    private final PdfRepository pdfRepository;
    private final UserRepository userRepository;

    @Override
    public FileExtensionResponse getFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) throw new NoFileFoundException("File cannot be null");

        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1) throw new NoFileExtensionFoundException("No file extension found");

        String extension = filename.substring(lastIndex + 1).toLowerCase();
        return StudyMaterialMapper.mapToFileExtensionResponse(extension, "File extension found");
    }

    @Override
    public ReadDocumentResponse navigateToProperFileExtension(NavigateToProperFileExtensionRequest request) throws Exception {
        Optional<User> existingUser = userRepository.findById(request.getUserId());
        if (existingUser.isEmpty()) throw new UserNotFoundException("User not found");

        FileExtensionResponse fileExtension = getFileExtension(request.getFile());
        String extension = fileExtension.getFileExtension();

        ReadDocumentResponse response;
        PageCountResponse pageCountResponse;

        switch (extension) {
            case "txt", "csv" -> {
                response = readTextFile(request.getFile());
                pageCountResponse = getTextFilePageCount(request.getFile());
            }
            case "pdf" -> {
                response = readPDF(request.getFile());
                pageCountResponse = getPDFPageCount(request.getFile());
            }
            case "docx", "doc" -> {
                response = readWordDocument(request.getFile());
                pageCountResponse = getWordPageCount(request.getFile());
            }
            case "xlsx", "xls" -> {
                response = readExcelFile(request.getFile());
                pageCountResponse = getExcelSheetCount(request.getFile());
            }
            default -> throw new FileExtensionNotSupportedException("Unsupported file extension: " + extension);
        }

        saveFileToDatabase(request.getUserId(), request.getFile(), extension, response.getText(), pageCountResponse.getNumberOfPages());

        return response;
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

    private void saveFileToDatabase(String userId, MultipartFile file, String extension, String content, int numberOfPages) {
        try {
            CreatePdfRequest request = new CreatePdfRequest();
            request.setUserId(userId);
            request.setFileName(file.getOriginalFilename());
            request.setExtension(extension);
            request.setContent(content);
            request.setSize((int) file.getSize());
            request.setNumberOfPages(numberOfPages);

            Pdf pdf = PdfMapper.mapToPdf(request);
            pdfRepository.save(pdf);
            log.info("File '{}' saved successfully for user {}", file.getOriginalFilename(), userId);
        } catch (Exception e) {
            log.error("Failed to save file to database: {}", e.getMessage());
            throw new RuntimeException("Failed to save file to database");
        }
    }

    private PageCountResponse getExcelSheetCount(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            return StudyMaterialMapper.mapToPageCountResponse(
                    workbook.getNumberOfSheets(),
                    "Page was counted successfully"
            );
        } catch (Exception e) {
            throw new FileExtensionNotSupportedException("File not supported: " + e.getMessage());
        }
    }

    private PageCountResponse getWordPageCount(MultipartFile file) {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            return StudyMaterialMapper.mapToPageCountResponse(
                    document.getParagraphs().size(),
                    "Page was counted successfully"
            );
        } catch (Exception e) {
            throw new FileExtensionNotSupportedException("File not supported: " + e.getMessage());
        }
    }

    private PageCountResponse getPDFPageCount(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            return StudyMaterialMapper.mapToPageCountResponse(
                    document.getNumberOfPages(),
                    "Page was counted successfully"
            );
        } catch (Exception e) {
            throw new FileExtensionNotSupportedException("File not supported: " + e.getMessage());
        }
    }

    private PageCountResponse getTextFilePageCount(MultipartFile file) throws Exception {
        int linesPerPage = 50;
        double lines = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            while (reader.readLine() != null) lines++;
        }
        int result = (int) Math.ceil(lines / linesPerPage);
        return StudyMaterialMapper.mapToPageCountResponse(result, "Page was counted successfully");
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
            throw new DocumentNotReadException("Error while reading study material");
        }

        return StudyMaterialMapper.mapToReadDocumentResponse(content.toString(), "Excel file was read successfully");
    }

    private ReadDocumentResponse readWordDocument(MultipartFile file) {
        StringBuilder content = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            document.getParagraphs().forEach(p -> content.append(p.getText()).append("\n"));
        } catch (Exception e) {
            throw new DocumentNotReadException("Error while reading study material");
        }
        return StudyMaterialMapper.mapToReadDocumentResponse(content.toString(), "Doc was read successfully");
    }

    private ReadDocumentResponse readPDF(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return StudyMaterialMapper.mapToReadDocumentResponse(stripper.getText(document), "PDF was read successfully");
        } catch (Exception e) {
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
            throw new DocumentNotReadException("Error while reading study material");
        }
        return StudyMaterialMapper.mapToReadDocumentResponse(content.toString(), "Text file was read successfully");
    }
}
