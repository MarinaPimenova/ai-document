package com.training.app.embedding.store;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;

    public void storeToVectorStore(String info, String imageUrl) {
        // Convert to document and embed
        Document doc = new Document(info);
        doc.getMetadata()
                .put("source", imageUrl);

        TokenTextSplitter splitter = new TokenTextSplitter();
        vectorStore.accept(splitter.apply(List.of(doc)));
    }

    public void storeToVectorStore(DocumentReader documentReader) {
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        vectorStore.accept(textSplitter.apply(documentReader.get()));
    }

    public List<Document> similaritySearch(String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(5)
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }
}
