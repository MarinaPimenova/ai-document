package com.training.app.rag.service;

import com.training.app.api.dto.AgentPayload;
import com.training.app.api.dto.DocumentAgentResponse;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RagService Unit Tests")
class RagServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec chatClientRequestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagService(vectorStore, chatMemory, chatClient);
    }

    @Ignore
    
    @DisplayName("Should generate response with valid conversation and question")
    void testGenerateSuccess() {
        // Arrange
        String conversationId = UUID.randomUUID().toString();
        Long questionId = 1L;
        String question = "How to configure AWS EKS ALB?";
        AgentPayload payload = new AgentPayload(questionId, question);

        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.advisors(anyList())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("AWS EKS ALB configuration guide...");

        // Act
        DocumentAgentResponse response = ragService.generate(conversationId, payload);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.getConversationId()).isEqualTo(conversationId);
                    assertThat(r.getQuestionId()).isEqualTo(questionId);
                    assertThat(r.getTermList()).isEqualTo(question);
                    assertThat(r.getSummary()).isEqualTo("AWS EKS ALB configuration guide...");
                    assertThat(r.getAgentType()).isEqualTo(DocumentAgentResponse.AgentType.DOCUMENT);
                });

        verify(chatClient, times(1)).prompt(anyString());
        verify(callResponseSpec, times(1)).content();
    }

    @Ignore
    
    @DisplayName("Should generate new session UUID when conversation ID is '1'")
    void testGenerateWithNewSessionId() {
        // Arrange
        String newSessionConversationId = "1";
        Long questionId = 2L;
        String question = "Test question";
        AgentPayload payload = new AgentPayload(questionId, question);

        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.advisors(anyList())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Response content");

        // Act
        DocumentAgentResponse response = ragService.generate(newSessionConversationId, payload);

        // Assert
        assertThat(response.getConversationId())
                .isNotEqualTo("1")
                .isNotNull()
                .hasSize(36); // UUID format length
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when conversationId is null")
    void testGenerateWithNullConversationId() {
        // Arrange
        Long questionId = 3L;
        String question = "Test question";
        AgentPayload payload = new AgentPayload(questionId, question);

        // Act & Assert
        assertThatThrownBy(() -> ragService.generate(null, payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("conversationId cannot be null");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when question is null")
    void testGenerateWithNullQuestion() {
        // Arrange
        String conversationId = UUID.randomUUID().toString();
        Long questionId = 4L;
        AgentPayload payload = new AgentPayload(questionId, null);

        // Act & Assert
        assertThatThrownBy(() -> ragService.generate(conversationId, payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("question cannot be null");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when questionId is null")
    void testGenerateWithNullQuestionId() {
        // Arrange
        String conversationId = UUID.randomUUID().toString();
        String question = "Test question";
        AgentPayload payload = new AgentPayload(null, question);

        // Act & Assert
        assertThatThrownBy(() -> ragService.generate(conversationId, payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("questionId cannot be null");
    }

    @Ignore
    
    @DisplayName("Should set correct source and document sets in response")
    void testGenerateResponseStructure() {
        // Arrange
        String conversationId = UUID.randomUUID().toString();
        AgentPayload payload = new AgentPayload(5L, "Question about Ingress controller");

        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.advisors(anyList())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Detailed response");

        // Act
        DocumentAgentResponse response = ragService.generate(conversationId, payload);

        // Assert
        assertThat(response)
                .extracting(DocumentAgentResponse::getSourceSet)
                .isNotNull();

        assertThat(response)
                .extracting(DocumentAgentResponse::getDocumentSet)
                .isNotNull();
    }

    @Ignore
    
    @DisplayName("Should preserve question in termList")
    void testGeneratePreservesQuestion() {
        // Arrange
        String conversationId = UUID.randomUUID().toString();
        String expectedQuestion = "How to setup Ingress controller with ALB?";
        AgentPayload payload = new AgentPayload(6L, expectedQuestion);

        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.advisors(anyList())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Response");

        // Act
        DocumentAgentResponse response = ragService.generate(conversationId, payload);

        // Assert
        assertThat(response.getTermList()).isEqualTo(expectedQuestion);
    }

}
