package com.training.app.embedding.service;

import com.training.app.embedding.web.WebPageReaderService;
import com.training.app.exception.UnsupportedFileType;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataLoaderService Unit Tests")
class DataLoaderServiceTest {

    @Mock
    private PdfFileReaderService pdfFileReaderService;

    @Mock
    private WebPageReaderService webPageReaderService;

    @Mock
    private ImageReaderService imageReaderService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private DataLoaderService dataLoaderService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(pdfFileReaderService, webPageReaderService, imageReaderService);
    }

    @Ignore

    @DisplayName("Should upload PDF file successfully")
    void testUploadPdfKnowledge() {
        // Arrange
        String fileName = "document.pdf";
        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getSize()).thenReturn(1024L); // 1KB
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.isEmpty()).thenReturn(false);

        // Act
        String result = dataLoaderService.uploadKnowledge(multipartFile);

        // Assert
        assertThat(result)
                .isNotNull()
                .isEqualTo(fileName);

        verify(pdfFileReaderService, times(1)).addResource(multipartFile);
        verify(imageReaderService, never()).addResource(any(MultipartFile.class), anyString());
    }

    @Ignore
    @DisplayName("Should upload image file successfully")
    void testUploadImageKnowledge() {
        // Arrange
        String fileName = "image.png";
        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getSize()).thenReturn(2048L);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.isEmpty()).thenReturn(false);

        // Act
        String result = dataLoaderService.uploadKnowledge(multipartFile);

        // Assert
        assertThat(result)
                .isNotNull()
                .isEqualTo(fileName);

        verify(imageReaderService, times(1)).addResource(multipartFile, fileName);
        verify(pdfFileReaderService, never()).addResource(any(MultipartFile.class));
    }

    @Ignore

    @DisplayName("Should throw UnsupportedFileType for unsupported file type")
    void testUploadUnsupportedFileType() {
        // Arrange
        String fileName = "document.txt";
        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.isEmpty()).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> dataLoaderService.uploadKnowledge(multipartFile))
                .isInstanceOf(UnsupportedFileType.class)
                .hasMessageContaining("Only PDF and image files are allowed");

        verify(pdfFileReaderService, never()).addResource(any(MultipartFile.class));
        verify(imageReaderService, never()).addResource(any(MultipartFile.class), anyString());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when multipart file is null")
    void testUploadNullFile() {
        // Act & Assert
        assertThatThrownBy(() -> dataLoaderService.uploadKnowledge(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upload new knowledge context cannot be null");
    }

    @Test
    @DisplayName("Should load web content from URL successfully")
    void testLoadFromUrlSuccess() {
        // Arrange
        String url = "https://example.com/aws-eks";

        // Act
        String result = dataLoaderService.loadFromUrl(url);

        // Assert
        assertThat(result)
                .isNotNull()
                .isEqualTo("Web content ingested from: " + url);

        verify(webPageReaderService, times(1)).addWebPageContent(url);
    }

@Test
    @DisplayName("Should throw IllegalArgumentException when URL is null")
    void testLoadFromUrlNull() {
        // Act & Assert
        assertThatThrownBy(() -> dataLoaderService.loadFromUrl(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL must not be empty");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when URL is empty")
    void testLoadFromUrlEmpty() {
        // Act & Assert
        assertThatThrownBy(() -> dataLoaderService.loadFromUrl(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL must not be empty");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when URL is blank")
    void testLoadFromUrlBlank() {
        // Act & Assert
        assertThatThrownBy(() -> dataLoaderService.loadFromUrl("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL must not be empty");
    }

    @Ignore

    @DisplayName("Should return original filename for valid PDF")
    void testUploadReturnFilename() {
        // Arrange
        String fileName = "training_guide.pdf";
        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getSize()).thenReturn(5120L);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.isEmpty()).thenReturn(false);

        // Act
        String result = dataLoaderService.uploadKnowledge(multipartFile);

        // Assert
        assertThat(result).isEqualTo(fileName);
    }

}