package com.training.app.controller;

import com.training.app.api.DeferredResultService;
import com.training.app.api.dto.AgentPayload;
import com.training.app.api.dto.DocumentAgentResponse;
import com.training.app.embedding.service.DataLoaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@ResponseBody
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DocumentAgentController {

    private final DataLoaderService dataLoaderService;

    private final DeferredResultService deferredResultService;

    @PostMapping(value = "/docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<DocumentAgentResponse>> docs(
            @RequestParam String conversationId,
            @RequestBody AgentPayload request,
            @Value("${agent.deferred-result-timeout:66000}") Long deferredResultTimeout) {
        return deferredResultService.getDeferredResult(conversationId, request, deferredResultTimeout);
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadKnowledge(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(dataLoaderService.uploadKnowledge(file));
    }

    @PostMapping("/load-url")
    public ResponseEntity<String> loadUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        return ResponseEntity.ok(dataLoaderService.loadFromUrl(url));
    }

}
