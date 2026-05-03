package com.training.app.embedding.web;

import com.training.app.embedding.service.ImageReaderService;
import com.training.app.embedding.store.VectorStoreService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class WebPageReaderService {

    private final ImageReaderService imageReaderService;
    private final VectorStoreService vectorStoreService;
    private final HtmlTableExtractorService htmlTableExtractorService;
    private final ChatClient chatTableClient;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
    private final SimpleLoggerAdvisor simpleLoggerAdvisor;

    public WebPageReaderService(
            VectorStore vectorStore,
            ImageReaderService imageReaderService,
            VectorStoreService vectorStoreService,
            HtmlTableExtractorService htmlTableExtractorService,
            ChatClient chatTableClient) {

        this.imageReaderService = imageReaderService;
        this.vectorStoreService = vectorStoreService;
        this.htmlTableExtractorService = htmlTableExtractorService;
        this.chatTableClient = chatTableClient;
        this.questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore).build();
        this.simpleLoggerAdvisor = new SimpleLoggerAdvisor();
    }

    public void addWebPageContent(String url) {
        try {
            addPlainWebPageContent(url);
            extractDiagrams(url);
            //addTablesDescriptionOfWebPageContent(url);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from URL: " + url + "Caused by: " + e.getMessage(), e);
        }
    }

    protected void addPlainWebPageContent(String url) throws IOException {
        Document htmlDoc = Jsoup.connect(url).get();
        // Extract readable content
        String textContent = htmlDoc.select("article, main, body").text(); // adjust selector as needed
        if (textContent.isBlank()) {
            throw new RuntimeException("No readable content found at URL: " + url);
        }
        // Convert into Document list
        vectorStoreService.storeToVectorStore(textContent, url);
    }

    protected List<String> extractImageUrlsFromPage(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        return doc.select("img")
                .stream()
                .map(img -> img.absUrl("src"))
                .filter(src -> src.endsWith(".png") || src.endsWith(".jpg"))
                .toList();
    }

    protected void extractDiagrams(String webpageUrl) throws IOException {
        List<String> imageUrls = extractImageUrlsFromPage(webpageUrl);
        for (String imageUrl : imageUrls) {
            imageReaderService.addResource(imageUrl);
        }
    }

    protected void addTablesDescriptionOfWebPageContent(String url) throws IOException {

        String tableHtml = htmlTableExtractorService.extractTablesAsHtml(url);

        String prompt = """
                Please analyze and summarize the following HTML tables.
                Convert data into meaningful descriptions that can be embedded for search.
                HTML:
                """ + tableHtml;
        String content = chatTableClient
                .prompt(prompt)
                .advisors(
                        questionAnswerAdvisor,
                        simpleLoggerAdvisor)
                .call()
                .content();

        // Now embed the response into vector store
        // Convert into Document list
        vectorStoreService.storeToVectorStore(content, url);
    }

}

