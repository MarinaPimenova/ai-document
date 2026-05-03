package com.training.app.embedding.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VectorStoreService Unit Tests")
class VectorStoreServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private DocumentReader documentReader;

    @InjectMocks
    private VectorStoreService vectorStoreService;

    @BeforeEach
    void setUp() {
        reset(vectorStore, documentReader);
    }

    @Test
    @DisplayName("Should store text to vector store with metadata")
    void testStoreToVectorStoreWithText() {
        // Arrange
        String content = "AWS EKS with ALB and Ingress configuration";
        String imageUrl = "eks-guide.pdf";

        // Act
        vectorStoreService.storeToVectorStore(content, imageUrl);

        // Assert
        verify(vectorStore, times(1)).accept(argThat(documents ->
                documents != null && !documents.isEmpty() &&
                        documents.get(0).getText().contains(content) &&
                        documents.get(0).getMetadata().get("source").equals(imageUrl)
        ));
    }

    @Test
    @DisplayName("Should store document reader content to vector store")
    void testStoreToVectorStoreWithDocumentReader() {
        // Arrange
        Document doc1 = new Document("Kubernetes configuration");
        Document doc2 = new Document("Docker basics");
        when(documentReader.get()).thenReturn(List.of(doc1, doc2));

        // Act
        vectorStoreService.storeToVectorStore(documentReader);

        // Assert
        verify(vectorStore, times(1)).accept(any());
        verify(documentReader, times(1)).get();
    }

    @Test
    @DisplayName("Should perform similarity search with correct request parameters")
    void testSimilaritySearch() {
        // Arrange
        String query = "ALB Ingress controller configuration";
        Document resultDoc = new Document("How to configure ALB with Ingress");
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(resultDoc));

        // Act
        List<Document> results = vectorStoreService.similaritySearch(query);

        // Assert
        assertThat(results)
                .isNotNull()
                .hasSize(1)
                .contains(resultDoc);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(captor.capture());

        SearchRequest request = captor.getValue();
        assertThat(request.getQuery()).isEqualTo(query);
        assertThat(request.getTopK()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should return empty list when no similar documents found")
    void testSimilaritySearchEmpty() {
        // Arrange
        String query = "Non-existent topic";
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        // Act
        List<Document> results = vectorStoreService.similaritySearch(query);

        // Assert
        assertThat(results)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Should set document metadata correctly when storing text")
    void testStoreToVectorStoreMetadata() {
        // Arrange
        String content = "Test document content";
        String source = "test-source";

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);

        // Act
        vectorStoreService.storeToVectorStore(content, source);

        // Assert
        verify(vectorStore).accept(captor.capture());
        List<Document> capturedDocs = captor.getValue();

        assertThat(capturedDocs)
                .isNotEmpty()
                .allSatisfy(doc ->
                        assertThat(doc.getMetadata())
                                .containsEntry("source", source)
                );
    }

    @Test
    @DisplayName("Should handle multiple documents from DocumentReader")
    void testStoreMultipleDocumentsFromReader() {
        // Arrange
        List<Document> documents = List.of(
                new Document("EKS setup"),
                new Document("ALB configuration"),
                new Document("Ingress controller")
        );
        when(documentReader.get()).thenReturn(documents);

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);

        // Act
        vectorStoreService.storeToVectorStore(documentReader);

        // Assert
        verify(vectorStore).accept(captor.capture());
        List<Document> capturedDocs = captor.getValue();

        assertThat(capturedDocs)
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should search with top K results parameter")
    void testSimilaritySearchTopK() {
        // Arrange
        String query = "Kubernetes setup";
        List<Document> mockResults = List.of(
                new Document("K8s tutorial"),
                new Document("Container orchestration")
        );
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(mockResults);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);

        // Act
        vectorStoreService.similaritySearch(query);

        // Assert
        verify(vectorStore).similaritySearch(captor.capture());
        assertThat(captor.getValue().getTopK()).isEqualTo(5);
    }

}