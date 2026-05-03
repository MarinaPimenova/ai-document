package com.training.app;

import com.training.app.api.dto.AgentPayload;
import com.training.app.api.dto.DocumentAgentResponse;
import com.training.app.embedding.store.VectorStoreService;
import com.training.app.rag.service.RagService;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

//@SpringBootTest
@Import(TestcontainersConfiguration.class)
//@DisplayName("Semantic Search Integration Tests with Testcontainers")
class SemanticSearchIntegrationTest {

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private RagService ragService;

    private static final String AWS_EKS_CONTENT = """
            AWS Elastic Kubernetes Service (EKS) Configuration Guide
            
            To configure AWS EKS with Application Load Balancer (ALB) and Ingress Controller:
            
            1. Create an EKS cluster using AWS Console or CLI
            2. Install AWS Load Balancer Controller using Helm
            3. Create an IAM policy for the controller
            4. Deploy the controller to your cluster
            5. Configure Ingress resources to use the ALB class
            
            The ALB Ingress Controller automatically provisions AWS Application Load Balancers 
            when you create Kubernetes Ingress resources with the alb.ingress.kubernetes.io 
            annotation. This allows external traffic to reach your services running on EKS.
            
            Prerequisites:
            - AWS Account with appropriate permissions
            - kubectl configured to access your EKS cluster
            - Helm installed (version 3 or later)
            - IAM OIDC provider configured for your cluster
            
            Configuration steps include setting up security groups, target groups, and 
            configuring SSL/TLS certificates through AWS Certificate Manager.
            """;

    private static final String ALB_INGRESS_CONTENT = """
            Configuring ALB with Kubernetes Ingress Controller
            
            The AWS Load Balancer Controller manages AWS Elastic Load Balancers (ELB) for you.
            It satisfies Kubernetes Ingress resources by provisioning Application Load Balancers.
            
            Key features:
            - Automatically provisions AWS Application Load Balancers
            - Supports Ingress attributes for fine-grained control
            - Integrates with AWS services like ACM for certificates
            - Supports multiple availability zones
            - Automatic health checks and auto-scaling
            
            Installation:
            1. Add the EKS chart repository
               helm repo add eks https://aws.github.io/eks-charts
            2. Install the controller
               helm install aws-load-balancer-controller eks/aws-load-balancer-controller \\
                 -n kube-system --set clusterName=my-cluster
            3. Verify installation
               kubectl get pods -n kube-system
            
            Ingress Configuration Example:
            - apiVersion: networking.k8s.io/v1
            - kind: Ingress
            - metadata:
            -   name: example-ingress
            -   annotations:
            -     alb.ingress.kubernetes.io/scheme: internet-facing
            -     alb.ingress.kubernetes.io/target-type: ip
            - spec:
            -   ingressClassName: alb
            -   rules:
            -   - host: example.com
            -     http:
            -       paths:
            -       - path: /
            -         pathType: Prefix
            -         backend:
            -           service:
            -             name: my-service
            -             port:
            -               number: 80
            """;

    private static final String KUBERNETES_BASICS = """
            Kubernetes Fundamentals and Best Practices
            
            Kubernetes is a portable, extensible, open-source platform for managing 
            containerized workloads and services. It provides declarative configuration 
            and automation for container deployment, scaling, and management.
            
            Core concepts:
            - Pods: Smallest deployable units in Kubernetes
            - Services: Abstract way to expose applications running on Pods
            - Deployments: Declarative updates for Pods and ReplicaSets
            - ConfigMaps and Secrets: Manage configuration and sensitive data
            - Namespaces: Virtual clusters for multi-tenancy
            - Persistent Volumes: Storage abstraction layer
            
            Best practices for production:
            - Use resource limits and requests
            - Implement health checks (liveness and readiness probes)
            - Use namespaces for logical separation
            - Implement network policies for security
            - Use RBAC for access control
            - Monitor and log your applications
            """;

    @BeforeEach
    void setUp() {
        // Clear any existing data in the vector store
        vectorStore.delete(List.of());

        // Store initial test data
        storeTestDocuments();
    }

    private void storeTestDocuments() {
        // Store AWS EKS configuration document
        vectorStoreService.storeToVectorStore(AWS_EKS_CONTENT, "aws-eks-config.txt");

        // Store ALB Ingress Controller document
        vectorStoreService.storeToVectorStore(ALB_INGRESS_CONTENT, "alb-ingress-controller.txt");

        // Store Kubernetes basics document
        vectorStoreService.storeToVectorStore(KUBERNETES_BASICS, "kubernetes-basics.txt");
    }

    @Ignore //@Test
    @DisplayName("Should retrieve AWS EKS configuration from vector store via semantic search")
    void testSemanticSearchAwsEksConfiguration() {
        // Act
        List<Document> results = vectorStoreService.similaritySearch("How to configure AWS EKS cluster");

        // Assert
        assertThat(results)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(0)
                .anySatisfy(doc ->
                        assertThat(doc.getText()).contains("EKS", "Elastic Kubernetes Service")
                );
    }

    @Ignore //@Test
    @DisplayName("Should find documents related to ALB and Ingress Controller")
    void testSemanticSearchAlbIngress() {
        // Act
        List<Document> results = vectorStoreService.similaritySearch("ALB Ingress Controller configuration");

        // Assert
        assertThat(results)
                .isNotNull()
                .isNotEmpty()
                .anySatisfy(doc ->
                        assertThat(doc.getText()).contains("Application Load Balancer", "Ingress")
                );
    }

    @Ignore //@Test
    @DisplayName("Should retrieve documents related to Kubernetes concepts")
    void testSemanticSearchKubernetesConcepts() {
        // Act
        List<Document> results = vectorStoreService.similaritySearch("Kubernetes Pods and Services");

        // Assert
        assertThat(results)
                .isNotNull()
                .isNotEmpty()
                .anySatisfy(doc ->
                        assertThat(doc.getText()).contains("Kubernetes", "Pods")
                );
    }

    @Ignore //@Test
    @DisplayName("Should preserve document metadata after storage")
    void testDocumentMetadataPreservation() {
        // Act
        List<Document> results = vectorStoreService.similaritySearch("EKS configuration");

        // Assert
        assertThat(results)
                .isNotEmpty()
                .anySatisfy(doc ->
                        assertThat(doc.getMetadata())
                                .containsKey("source")
                                .extracting("source")
                                .asString()
                                .isNotEmpty()
                );
    }

    @Ignore //@Test
    @DisplayName("Should return multiple relevant documents for broad queries")
    void testSemanticSearchReturnsMultipleResults() {
        // Act
        List<Document> results = vectorStoreService.similaritySearch("kubernetes configuration");

        // Assert
        assertThat(results)
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(1)
                .hasSizeLessThanOrEqualTo(5); // Top K = 5
    }

    @Ignore //@Test
    @DisplayName("Should answer RAG question using stored documents")
    void testRagQuestionAnsweringWithStoredDocuments() {
        // Arrange
        String conversationId = UUID.randomUUID().toString();
        AgentPayload payload = new AgentPayload(
                1L,
                "How do I configure AWS EKS with ALB Ingress controller?"
        );

        // Act
        DocumentAgentResponse response = ragService.generate(conversationId, payload);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.getConversationId()).isEqualTo(conversationId);
                    assertThat(r.getQuestionId()).isEqualTo(1L);
                    assertThat(r.getTermList()).contains("EKS", "ALB", "Ingress");
                    assertThat(r.getSummary()).isNotNull().isNotEmpty();
                    assertThat(r.getAgentType()).isEqualTo(DocumentAgentResponse.AgentType.DOCUMENT);
                });
    }

    @Ignore //@Test
    @DisplayName("Should generate different conversations with separate conversation IDs")
    void testMultipleConversationsWithDifferentIds() {
        // Arrange
        String conversationId1 = UUID.randomUUID().toString();
        String conversationId2 = UUID.randomUUID().toString();

        AgentPayload payload1 = new AgentPayload(1L, "What is EKS?");
        AgentPayload payload2 = new AgentPayload(2L, "What is ALB?");

        // Act
        DocumentAgentResponse response1 = ragService.generate(conversationId1, payload1);
        DocumentAgentResponse response2 = ragService.generate(conversationId2, payload2);

        // Assert
        assertThat(response1.getConversationId()).isEqualTo(conversationId1);
        assertThat(response2.getConversationId()).isEqualTo(conversationId2);
        assertThat(response1.getConversationId()).isNotEqualTo(response2.getConversationId());
    }

    @Ignore //@Test
    @DisplayName("Should retrieve relevant documents for configuration queries")
    void testRetrieveDocumentsForConfigurationQueries() {
        // Act
        List<Document> results = vectorStoreService.similaritySearch(
                "How to install and configure AWS Load Balancer Controller on EKS"
        );

        // Assert
        assertThat(results)
                .isNotNull()
                .isNotEmpty()
                .anySatisfy(doc ->
                        assertThat(doc.getText())
                                .contains("Load Balancer Controller")
                );
    }

    @Ignore //@Test
    @DisplayName("Should handle complex technical queries through semantic search")
    void testComplexTechnicalQuery() {
        // Act
        List<Document> results = vectorStoreService.similaritySearch(
                "Setting up SSL/TLS certificates for Kubernetes Ingress with AWS ACM"
        );

        // Assert
        assertThat(results)
                .isNotNull()
                .isNotEmpty();
    }

    @Ignore //@Test
    @DisplayName("Should store and retrieve documents with specific source metadata")
    void testDocumentStorageWithSourceMetadata() {
        // Arrange
        String testContent = "Test Kubernetes configuration content";
        String testSource = "test-document.txt";

        // Act
        vectorStoreService.storeToVectorStore(testContent, testSource);
        List<Document> results = vectorStoreService.similaritySearch("Kubernetes");

        // Assert
        assertThat(results)
                .isNotEmpty()
                .anySatisfy(doc ->
                        assertThat(doc.getMetadata())
                                .extracting("source")
                                .isEqualTo(testSource)
                );
    }

    @Ignore //@Test
    @DisplayName("Should return documents sorted by relevance")
    void testDocumentsReturnedByRelevance() {
        // Act
        List<Document> results = vectorStoreService.similaritySearch("EKS ALB Ingress");

        // Assert
        assertThat(results)
                .isNotNull()
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(5); // Top K limit
    }

    @Ignore //@Test
    @DisplayName("Should answer follow-up questions in same conversation")
    void testFollowUpQuestionsInSameConversation() {
        // Arrange
        String conversationId = UUID.randomUUID().toString();
        AgentPayload firstQuestion = new AgentPayload(1L, "What is AWS EKS?");
        AgentPayload followUpQuestion = new AgentPayload(2L, "How do I configure the ALB controller?");

        // Act
        DocumentAgentResponse firstResponse = ragService.generate(conversationId, firstQuestion);
        DocumentAgentResponse followUpResponse = ragService.generate(conversationId, followUpQuestion);

        // Assert
        assertThat(firstResponse.getConversationId())
                .isEqualTo(followUpResponse.getConversationId())
                .isEqualTo(conversationId);

        assertThat(firstResponse.getQuestionId()).isNotEqualTo(followUpResponse.getQuestionId());
    }

}