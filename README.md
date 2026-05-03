# AI Document - RAG Microservice

A Spring Boot 3.x microservice that implements Retrieval-Augmented Generation (RAG) 
for intelligent document processing and question answering. 
The service integrates with the AI Chatbot Orchestrator via RESTful HTTP APIs.

## Overview

**ai-document** processes documents (DOCX, PDF, and web content), extracts embeddings using vector stores, and enables semantic search to provide context-aware responses to user queries through RAG mechanisms.

## Key Features

- **Document Upload & Processing**: Upload DOCX and PDF files with automatic parsing, chunking, and embedding generation
- **Web Content Ingestion**: Load and process HTML websites with ETL pipeline and vector storage
- **RAG-based Query Resolution**: Process user questions with semantic search against stored document embeddings
- **Vector Store Integration**: PgVector-backed storage for high-dimensional embeddings (HNSW indexing)
- **Async Processing**: DeferredResult-based asynchronous API responses for long-running operations
- **Multi-AI Model Support**: Compatible with OpenAI and Mistral AI models
- **Conversation Context**: Per-conversation session management via conversation ID

## Technology Stack

- **Java**: 21
- **Framework**: Spring Boot 3.5.x
- **Vector Database**: PostgreSQL with PgVector extension
- **AI Models**: OpenAI (GPT-4), Mistral AI
- **Embedding Model**: text-embedding-ada-002
- **HTTP Client**: RestTemplate, HttpClient5
- **Document Parsing**: Apache Tika, JSoup (HTML), Spring AI PDF/Markdown Readers
- **Build**: Maven
- **Async**: Spring DeferredResult with configurable timeouts

## API Endpoints

All endpoints are prefixed with `/api/v1`

### Upload Documents

POST /upload Content-Type: multipart/form-data

Request:

    file: MultipartFile (DOCX, PDF)

Response:

    200 OK: Success message

Fetches and processes HTML content, 
applies ETL transformation, 
and persists embeddings to vector store.

### Query Documents (RAG)

POST /docs?conversationId=<UUID> Content-Type: application/json

Request Body: { "question": "user question text", ...additional payload fields }

Response:

    200 OK: DocumentAgentResponse with AI-generated answer

Executes RAG pipeline: semantic search across embeddings → context retrieval → LLM prompt generation → response.

**Async Processing**: Requests are processed asynchronously with configurable timeout (default: 66 seconds).

## Configuration

Key properties in `application.yml`:

```yaml
server:
  port: 8099
  
spring:
  ai:
    openai:
      api-key: ${OPEN_AI_API_KEY}
      chat:
        model: gpt-4.1-mini-2025-04-14
    vectorstore:
      pgvector:
        dimensions: 1024
        index-type: HNSW
        distance-type: COSINE_DISTANCE
        
datasource:
  pgvector:
    jdbcUrl: ${PGVECTOR_URL:jdbc:postgresql://localhost:5434/embd}
    username: ${POSTGRES_USER:postgres}
    password: ${POSTGRES_PASSWORD:postgres}
```
### Environment Variables
Variable	Required	Default	Purpose
OPEN_AI_API_KEY	Yes	-	OpenAI API authentication
OPEN_AI_ENDPOINT	No	https://ai-proxy.lab.epam.com	OpenAI proxy endpoint
MISTRAL_AI_API_KEY	No	-	Mistral AI API key (optional)
PGVECTOR_URL	Yes	jdbc:postgresql://localhost:5434/embd	PostgreSQL connection URL
POSTGRES_USER	Yes	X	Database username
POSTGRES_PASSWORD	Yes	X	Database password
CHAT_MODEL	No	gpt-4.1-mini-2025-04-14	LLM model identifier

## Building & Running
### Prerequisites

    Java 21+
    PostgreSQL 14+ with PgVector extension
    Maven 3.8+
    Valid OpenAI API credentials

### Build
```shell
./mvnw clean package
```
### Run Locally
```shell
./mvnw spring-boot:run
```
The service will start on http://localhost:8099
### Docker
```shell
docker build -t ai-document:1.0.0 .
docker run -p 8099:8099 \
  -e OPEN_AI_API_KEY=<key> \
  -e PGVECTOR_URL=jdbc:postgresql://postgres:5434/embd \
  ai-document:1.0.0
```
## API Documentation
Swagger UI available at: http://localhost:8099/swagger-ui.html

# Architecture
```
Request → DocumentAgentController
         ↓
    RAG Service Pipeline
         ↓
    Vector Store Query (PgVector)
         ↓
    LLM Prompt Generation
         ↓
    Response Generation (OpenAI/Mistral)
         ↓
    DeferredResult Response
```

## Integration with AI Chatbot Orchestrator

This microservice acts as a knowledge retrieval engine for the AI Chatbot Orchestrator:

    Orchestrator sends user queries to /api/v1/docs endpoint
    ai-document performs semantic search and context retrieval
    Service returns enriched context and AI-generated response
    Orchestrator incorporates response into conversation flow

## Project Structure
```
src/main/java/com/training/app/
├── controller/          # REST API controllers
│   └── DocumentAgentController
├── rag/                 # RAG service implementation
├── embedding/           # Document embedding & parsing
├── api/                 # DTOs and service interfaces
├── configuration/       # Spring configuration
├── util/                # Utility classes
└── exception/           # Exception handling
```
## Health & Monitoring

    Health Endpoint: http://localhost:8099/actuator/health
    Metrics: Available via /actuator/metrics
    API Docs: Accessible via /actuator

### Notes

    Maximum file upload size: 10MB
    Vector dimensions: 1024 (configurable for PostgresML compatibility)
    Session timeout: 60 minutes
    HTTP client timeouts: Connection 5s, Read 120s


---

### Clarifications & Recommendations

1. **System Prompt**: The `system-prompt-template.st` is referenced but its content/usage should be documented in your deployment guide
2. **Error Handling**: Document expected error responses and recovery procedures
3. **Rate Limiting**: No rate limiting configuration visible—consider adding if exposed to external orchestrators
4. **Version Strategy**: Current version is 1.0.0; establish versioning and deprecation policy
5. **Conversation Context**: Document how conversation state is maintained across requests
