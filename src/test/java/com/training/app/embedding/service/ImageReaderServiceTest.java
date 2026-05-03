package com.training.app.embedding.service;

import com.training.app.ai.config.anthropicai.OpenAiVisionClient;
import com.training.app.embedding.store.VectorStoreService;
import com.training.app.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageReaderService Unit Tests")
class ImageReaderServiceTest {

    @Mock
    private VectorStoreService vectorStoreService;

    @Mock
    private OpenAiVisionClient openAiVisionClient;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ImageReaderService imageReaderService;

    private static final String TEST_IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    @BeforeEach
    void setUp() {
        reset(vectorStoreService, openAiVisionClient);
    }

    @Test
    @DisplayName("Should convert image bytes to base64 string")
    void testGetImageAsBase64() {
        // Arrange
        byte[] imageBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

        // Act
        String base64 = imageReaderService.getImageAsBase64(imageBytes);

        // Assert
        assertThat(base64)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(Base64.getEncoder().encodeToString(imageBytes));
    }

    @Test
    @DisplayName("Should add image resource from multipart file successfully")
    void testAddResourceFromMultipartFile() throws IOException {
        // Arrange
        String imageName = "test-image.png";
        byte[] imageBytes = Base64.getDecoder().decode(TEST_IMAGE_BASE64);

        when(multipartFile.getBytes()).thenReturn(imageBytes);
        when(openAiVisionClient.ask(any())).thenReturn("This is a test image");

        // Act
        imageReaderService.addResource(multipartFile, imageName);

        // Assert
        verify(openAiVisionClient, times(1)).ask(any());
        verify(vectorStoreService, times(1)).storeToVectorStore(
                contains("test-image.png"),
                eq(imageName)
        );
    }

    @Test
    @DisplayName("Should throw StorageException when image read fails")
    void testAddResourceIOException() throws IOException {
        // Arrange
        String imageName = "test-image.png";
        when(multipartFile.getBytes()).thenThrow(new IOException("Failed to read file"));

        // Act & Assert
        assertThatThrownBy(() -> imageReaderService.addResource(multipartFile, imageName))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to read image")
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Should handle vision client exception gracefully")
    void testAddResourceWithVisionClientException() throws IOException {
        // Arrange
        String imageName = "test-image.png";
        byte[] imageBytes = Base64.getDecoder().decode(TEST_IMAGE_BASE64);

        when(multipartFile.getBytes()).thenReturn(imageBytes);
        when(openAiVisionClient.ask(any())).thenThrow(new RuntimeException("Vision API error"));

        // Act - Should not throw, logs error instead
        imageReaderService.addResource(multipartFile, imageName);

        // Assert
        verify(openAiVisionClient, times(1)).ask(any());
        verify(vectorStoreService, never()).storeToVectorStore(anyString(), anyString());
    }

    @Test
    @DisplayName("Should format answer correctly before storing to vector store")
    void testRecognizeImageFormatting() throws IOException {
        // Arrange
        String imageName = "test-image.png";
        byte[] imageBytes = Base64.getDecoder().decode(TEST_IMAGE_BASE64);
        String expectedAnswer = "A test diagram";

        when(multipartFile.getBytes()).thenReturn(imageBytes);
        when(openAiVisionClient.ask(any())).thenReturn(expectedAnswer);

        // Act
        imageReaderService.addResource(multipartFile, imageName);

        // Assert
        verify(vectorStoreService, times(1)).storeToVectorStore(
                argThat(arg -> arg.contains(imageName) && arg.contains(expectedAnswer)),
                eq(imageName)
        );
    }

    @Test
    @DisplayName("Should recognize image from base64 and store to vector store")
    void testRecognizeImageSuccess() {
        // Arrange
        String imageUrl = "test-image-url.png";
        String base64Image = TEST_IMAGE_BASE64;
        String visionAnswer = "AWS EKS cluster diagram";

        when(openAiVisionClient.ask(any())).thenReturn(visionAnswer);

        // Act
        imageReaderService.recognizeImage(base64Image, imageUrl);

        // Assert
        verify(openAiVisionClient, times(1)).ask(argThat(content ->
                content instanceof java.util.List
        ));
        verify(vectorStoreService, times(1)).storeToVectorStore(
                contains("test-image-url.png"),
                        //.contains("AWS EKS cluster diagram"),
                eq(imageUrl)
        );
    }

    @Test
    @DisplayName("Should create correct OpenAI Vision request format")
    void testCreateCorrectVisionRequestFormat() throws IOException {
        // Arrange
        String imageName = "eks-diagram.png";
        byte[] imageBytes = Base64.getDecoder().decode(TEST_IMAGE_BASE64);

        when(multipartFile.getBytes()).thenReturn(imageBytes);
        when(openAiVisionClient.ask(any())).thenReturn("Kubernetes diagram");

        // Act
        imageReaderService.addResource(multipartFile, imageName);

        // Assert
        verify(openAiVisionClient, times(1)).ask(argThat(content -> {
            if (content instanceof java.util.List) {
                java.util.List<?> list = content;
                return list.size() == 2; // text + image_url
            }
            return false;
        }));
    }

}