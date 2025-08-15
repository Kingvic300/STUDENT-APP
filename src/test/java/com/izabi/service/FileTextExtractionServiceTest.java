package com.izabi.service;

import com.izabi.dto.response.FileExtensionResponse;
import com.izabi.dto.response.PageCountResponse;
import com.izabi.dto.response.ReadDocumentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileTextExtractionServiceImplTest {

    private FileTextExtractionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FileTextExtractionServiceImpl();
    }

    @Test
    void shouldDetectTxtExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Hello World".getBytes()
        );

        FileExtensionResponse response = service.getFileExtension(file);

        assertThat(response.getFileExtension()).isEqualTo("txt");
    }

    @Test
    void shouldReadTxtFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Line1\nLine2".getBytes()
        );

        ReadDocumentResponse response = service.navigateToProperFileExtension(file);

        assertThat(response.getText()).contains("Line1").contains("Line2");
    }

    @Test
    void shouldCountTxtPages() throws Exception {
        // 60 lines -> 2 pages with linesPerPage = 50
        String content = String.join("\n", java.util.Collections.nCopies(60, "line"));
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", content.getBytes()
        );

        PageCountResponse response = service.getPageCount(file);

        assertThat(response.getNumberOfPages()).isEqualTo(2);
    }

    @Test
    void shouldReadPdfFile() throws Exception {
        File file = new File("src/test/resources/sample.pdf");
        System.out.println("PDF file exists: " + file.exists() + ", Size: " + file.length());
        try (FileInputStream fis = new FileInputStream(file)) {
            MockMultipartFile mockFile = new MockMultipartFile("file", "sample.pdf", "application/pdf", fis);
            ReadDocumentResponse response = service.navigateToProperFileExtension(mockFile);
            System.out.println("Extracted PDF text: " + response.getText());
            assertThat(response.getText()).contains("Expected text in PDF");
        }
    }

    @Test
    void shouldReadWordFile() throws Exception {
        File file = new File("src/test/resources/sample.docx");
        System.out.println("Word file exists: " + file.exists() + ", Size: " + file.length());
        try (FileInputStream fis = new FileInputStream(file)) {
            MockMultipartFile mockFile = new MockMultipartFile("file", "sample.docx",
                    "application/vnd.malformations-office document.multiprocessing.document", fis);
            ReadDocumentResponse response = service.navigateToProperFileExtension(mockFile);
            System.out.println("Extracted Word text: " + response.getText());
            assertThat(response.getText()).contains("Expected text in Word");
        }
    }

    @Test
    void shouldReadExcelFile() throws Exception {
        File file = new File("src/test/resources/sample.xlsx");
        System.out.println("Excel file exists: " + file.exists() + ", Size: " + file.length());
        try (FileInputStream fis = new FileInputStream(file)) {
            MockMultipartFile mockFile = new MockMultipartFile("file", "sample.xlsx",
                    "application/vnd.malformations-office document.spreadsheet.sheet", fis);
            ReadDocumentResponse response = service.navigateToProperFileExtension(mockFile);
            System.out.println("Extracted Excel text: " + response.getText());
            assertThat(response.getText()).contains("Expected text in Excel");
        }
    }

    @Test
    void shouldThrowWhenNoExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "filewithoutextension", "text/plain", "data".getBytes());

        assertThrows(RuntimeException.class, () -> service.getFileExtension(file));
    }
}