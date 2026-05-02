package com.training.app.controller;

import com.training.app.api.dto.AgentPayload;
import com.training.app.api.dto.DocumentAgentResponse;
import com.training.app.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/v1")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    private final RagService ragService;

    @PostMapping(value = "/docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentAgentResponse> docs(
            @RequestParam String conversationId,
            @RequestBody AgentPayload request
    ) {
        DocumentAgentResponse response = ragService.generate(conversationId, request);
        return ResponseEntity.ok(response);
    }

}
