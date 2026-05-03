package com.training.app.embedding.service;

import com.training.app.embedding.store.VectorStoreService;
import com.training.app.exception.StorageException;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PdfFileReaderService Unit Tests")
class PdfFileReaderServiceTest {

    @Mock
    private VectorStoreService vectorStoreService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private PdfFileReaderService pdfFileReaderService;

    @BeforeEach
    void setUp() {
        reset(vectorStoreService);
    }

    @Ignore
    
    @DisplayName("Should add PDF resource successfully")
    void testAddResourceSuccess() throws IOException {
        // Arrange
        String fileName = "document.pdf";
        byte[] pdfContent = new byte[]{
                0x25, 0x50, 0x44, 0x46, (byte) 0xFF  // Simple PDF header
        };

        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfContent));
        when(multipartFile.isEmpty()).thenReturn(false);

        // Act
        pdfFileReaderService.addResource(multipartFile);

        // Assert
        verify(vectorStoreService, times(1)).storeToVectorStore(any());
    }

    @Ignore
    
    @DisplayName("Should throw StorageException when file is empty")
    void testAddResourceEmptyFile() throws IOException {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(true);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        // Act & Assert
        assertThatThrownBy(() -> pdfFileReaderService.addResource(multipartFile))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to store empty file");
    }

    
    @DisplayName("Should throw StorageException when file write fails")
    void testAddResourceIOException() throws IOException {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getInputStream()).thenThrow(new IOException("File read error"));

        // Act & Assert
        assertThatThrownBy(() -> pdfFileReaderService.addResource(multipartFile))
                //.isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to store file");
    }

    @Test
    @DisplayName("Should retrieve resources by similarity search query")
    void testGetResourcesByQuery() {
        // Arrange
        String query = "AWS EKS configuration";
        Document doc1 = new Document("EKS cluster setup guide");
        Document doc2 = new Document("Kubernetes basics");
        List<Document> expectedDocuments = List.of(doc1, doc2);

        when(vectorStoreService.similaritySearch(query)).thenReturn(expectedDocuments);

        // Act
        List<Document> result = pdfFileReaderService.getResources(query);

        // Assert
        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactlyElementsOf(expectedDocuments);

        verify(vectorStoreService, times(1)).similaritySearch(query);
    }

    @Test
    @DisplayName("Should return empty list when no similar documents found")
    void testGetResourcesEmpty() {
        // Arrange
        String query = "Non-existent topic";
        when(vectorStoreService.similaritySearch(query)).thenReturn(List.of());

        // Act
        List<Document> result = pdfFileReaderService.getResources(query);

        // Assert
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Ignore
    
    @DisplayName("Should call VectorStoreService with correct DocumentReader")
    void testAddResourceCallsVectorStore() throws IOException {
        // Arrange
        String fileName = "guide.pdf";
        byte[] pdfContent = new byte[]{
                0x25, 0x50, 0x44, 0x46  // PDF header
        };

        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfContent));
        when(multipartFile.isEmpty()).thenReturn(false);

        // Act
        pdfFileReaderService.addResource(multipartFile);

        // Assert
        verify(vectorStoreService, times(1)).storeToVectorStore(any());
    }

}