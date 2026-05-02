package com.training.app.embadding.service;

import com.training.app.ai.config.anthropicai.OpenAiVisionClient;
import com.training.app.embadding.store.VectorStoreService;
import com.training.app.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageReaderService {
    private static final String ANSWER_FORMAT = "%s describes: %s";
    private final VectorStoreService vectorStoreService;
    private final OpenAiVisionClient openAiVisionClient;

    protected String getImageAsBase64(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    protected String downloadImageAsBase64(String imageUrl) {
        try (InputStream in = new URL(imageUrl).openStream()) {
            byte[] imageBytes = in.readAllBytes();
            return getImageAsBase64(imageBytes);
        } catch (IOException e) {
            throw new StorageException("Failed to read image", e);
        }
    }

    public void addResource(String imageUrl) {
        String base64Image = downloadImageAsBase64(imageUrl);
        recognizeImage(base64Image, imageUrl);
    }

    protected void recognizeImage(String base64Image, String imageUrl) {
        // Build the prompt
        String prompt = "What is in this image?";

        // Build OpenAI Vision message
        List<Map<String, Object>> content = List.of(
                Map.of("type", "text", "text", prompt),
                Map.of("type", "image_url", "image_url", Map.of(
                        "url", "data:image/png;base64," + base64Image
                ))
        );

        // Call OpenAI Vision API directly (since Spring AI may not yet support vision natively)
        try {
            String answer = openAiVisionClient.ask(content);
            vectorStoreService.storeToVectorStore(format(ANSWER_FORMAT, imageUrl, answer), imageUrl);
        } catch (RuntimeException ex) {
            log.error(ex.getMessage());
        }
    }

    public void addResource(MultipartFile file, String imageName) {
        // Convert image to base64
        String base64Image;
        try {
            base64Image = getImageAsBase64(file.getBytes());
        } catch (IOException e) {
            throw new StorageException("Failed to read image", e);
        }
        recognizeImage(base64Image, imageName);
    }

}
