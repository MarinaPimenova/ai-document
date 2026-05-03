package com.training.app.embedding.service;

import com.training.app.embedding.store.VectorStoreService;
import com.training.app.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfFileReaderService {

    private final VectorStoreService vectorStoreService;

    public void addResource(MultipartFile multipartFile) {
        File file = store(multipartFile);
        FileSystemResource resource = new FileSystemResource(file);
        ExtractedTextFormatter textFormatter = ExtractedTextFormatter.builder()
                .withNumberOfBottomTextLinesToDelete(3)
                .withNumberOfTopPagesToSkipBeforeDelete(1)
                .build();
        PdfDocumentReaderConfig pdfDocumentReaderConfig = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(textFormatter)
                .withPagesPerDocument(1)
                .build();

        PagePdfDocumentReader pagePdfDocumentReader =
                new PagePdfDocumentReader(resource, pdfDocumentReaderConfig);

        vectorStoreService.storeToVectorStore(pagePdfDocumentReader);
    }

    protected File store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            String uploadDir = "/uploads";
            Path filePath = Paths.get(uploadDir, file.getOriginalFilename());
            Files.createDirectories(filePath.getParent()); // Create directory if it doesn't exist
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toFile();
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    public List<Document> getResources(String query) {
        return vectorStoreService.similaritySearch(query);
    }
}
