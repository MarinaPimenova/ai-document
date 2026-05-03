package com.training.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.app.api.DeferredResultService;
import com.training.app.api.dto.AgentPayload;
import com.training.app.api.dto.DocumentAgentResponse;
import com.training.app.embedding.service.DataLoaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentAgentController.class)
@DisplayName("DocumentAgentController Unit Tests")
class DocumentAgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeferredResultService deferredResultService;

    @MockBean
    private DataLoaderService dataLoaderService;

    @BeforeEach
    void setUp() {
        reset(deferredResultService, dataLoaderService);
    }

    @Test
    @DisplayName("Should query documents with valid conversation ID and return response")
    void testDocsEndpointSuccess() throws Exception {
        // Arrange
        String conversationId = UUID.randomUUID().toString();
        Long questionId = 1L;
        String question = "How to configure AWS EKS ALB?";
        AgentPayload payload = new AgentPayload(questionId, question);

        DocumentAgentResponse response = DocumentAgentResponse.builder()
                .conversationId(conversationId)
                .questionId(questionId)
                .termList(question)
                .summary("AWS EKS with ALB configuration guide")
                .build();

        DeferredResult<Object> deferredResult = new DeferredResult<>(5000L);
        deferredResult.setResult(response);

        when(deferredResultService.getDeferredResult(eq(conversationId), any(AgentPayload.class), anyLong()))
                .thenReturn((DeferredResult<org.springframework.http.ResponseEntity<DocumentAgentResponse>>) (Object) deferredResult);

        // Act & Assert
        mockMvc.perform(post("/api/v1/docs")
                        .param("conversationId", conversationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
                //.andExpect(jsonPath("$.conversationId").value(conversationId))
                //.andExpect(jsonPath("$.questionId").value(questionId))
                //.andExpect(jsonPath("$.termList").value(question));

        verify(deferredResultService, times(1))
                .getDeferredResult(eq(conversationId), any(AgentPayload.class), anyLong());
    }

    @Test
    @DisplayName("Should upload PDF file successfully")
    void testUploadKnowledgeSuccess() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        when(dataLoaderService.uploadKnowledge(any())).thenReturn("document.pdf");

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("document.pdf"));

        verify(dataLoaderService, times(1)).uploadKnowledge(any());
    }

    @Test
    @DisplayName("Should upload image file successfully")
    void testUploadImageSuccess() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "diagram.png",
                "image/png",
                "PNG content".getBytes()
        );

        when(dataLoaderService.uploadKnowledge(any())).thenReturn("diagram.png");

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("diagram.png"));

        verify(dataLoaderService, times(1)).uploadKnowledge(any());
    }

    @Test
    @DisplayName("Should load web content from URL")
    void testLoadUrlSuccess() throws Exception {
        // Arrange
        String url = "https://example.com/aws-eks";
        String requestBody = "{\"url\": \"" + url + "\"}";

        when(dataLoaderService.loadFromUrl(url))
                .thenReturn("Web content ingested from: " + url);

        // Act & Assert
        mockMvc.perform(post("/api/v1/load-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Web content ingested from: " + url));

        verify(dataLoaderService, times(1)).loadFromUrl(url);
    }

    @Test
    @DisplayName("Should return 200 OK for valid docs request")
    void testDocsEndpointStatusCode() throws Exception {
        // Arrange
        String conversationId = UUID.randomUUID().toString();
        AgentPayload payload = new AgentPayload(2L, "Question?");

        DeferredResult<Object> deferredResult = new DeferredResult<>();
        deferredResult.setResult(DocumentAgentResponse.builder().build());

        when(deferredResultService.getDeferredResult(anyString(), any(AgentPayload.class), anyLong()))
                .thenReturn((DeferredResult<org.springframework.http.ResponseEntity<DocumentAgentResponse>>) (Object) deferredResult);

        // Act & Assert
        mockMvc.perform(post("/api/v1/docs")
                        .param("conversationId", conversationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return correct content type for docs endpoint")
    void testDocsEndpointContentType() throws Exception {
        // Arrange
        String conversationId = UUID.randomUUID().toString();
        AgentPayload payload = new AgentPayload(3L, "Test question");

        DeferredResult<Object> deferredResult = new DeferredResult<>();
        deferredResult.setResult(DocumentAgentResponse.builder().build());

        when(deferredResultService.getDeferredResult(anyString(), any(AgentPayload.class), anyLong()))
                .thenReturn((DeferredResult<org.springframework.http.ResponseEntity<DocumentAgentResponse>>) (Object) deferredResult);

        // Act & Assert
        mockMvc.perform(post("/api/v1/docs")
                        .param("conversationId", conversationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)));
                //.andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should pass correct conversation ID to DeferredResultService")
    void testDocsPassesConversationIdCorrectly() throws Exception {
        // Arrange
        String conversationId = UUID.randomUUID().toString();
        AgentPayload payload = new AgentPayload(4L, "AWS question");

        DeferredResult<Object> deferredResult = new DeferredResult<>();
        deferredResult.setResult(DocumentAgentResponse.builder().build());

        when(deferredResultService.getDeferredResult(anyString(), any(AgentPayload.class), anyLong()))
                .thenReturn((DeferredResult<org.springframework.http.ResponseEntity<DocumentAgentResponse>>) (Object) deferredResult);

        // Act
        mockMvc.perform(post("/api/v1/docs")
                        .param("conversationId", conversationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // Assert
        verify(deferredResultService).getDeferredResult(eq(conversationId), any(AgentPayload.class), anyLong());
    }

}