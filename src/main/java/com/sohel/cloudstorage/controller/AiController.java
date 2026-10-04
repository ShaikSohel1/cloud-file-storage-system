package com.sohel.cloudstorage.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.entity.DocumentInsightEntity;
import com.sohel.cloudstorage.service.AiService;
import com.sohel.cloudstorage.service.InsightService;
import com.sohel.cloudstorage.service.OcrService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final OcrService ocrService;
    private final InsightService insightService;

    @PostMapping("/files/{fileId}/summarize")
    public ResponseEntity<ApiResponse<String>> summarizeDocument(@PathVariable("fileId") UUID fileId) {
        String summary = aiService.summarizeDocument(fileId);
        return ResponseEntity.ok(ApiResponse.success("Document summarized", summary));
    }

    @PostMapping("/files/{fileId}/action-items")
    public ResponseEntity<ApiResponse<String>> extractActionItems(@PathVariable("fileId") UUID fileId) {
        String items = aiService.extractActionItems(fileId);
        return ResponseEntity.ok(ApiResponse.success("Action items extracted", items));
    }

    @PostMapping("/files/{fileId}/chat")
    public ResponseEntity<ApiResponse<String>> chatWithDocument(
            @PathVariable("fileId") UUID fileId,
            @RequestParam("prompt") String prompt) {
        String response = aiService.generateChatResponse(fileId, prompt);
        return ResponseEntity.ok(ApiResponse.success("Chat response generated", response));
    }

    @PostMapping("/files/{fileId}/ocr")
    public ResponseEntity<ApiResponse<String>> extractText(@PathVariable("fileId") UUID fileId) {
        String text = ocrService.extractTextFromImage(fileId);
        return ResponseEntity.ok(ApiResponse.success("Text extracted", text));
    }

    @PostMapping("/files/{fileId}/insights")
    public ResponseEntity<ApiResponse<DocumentInsightEntity>> generateInsights(@PathVariable("fileId") UUID fileId) {
        DocumentInsightEntity insights = insightService.generateInsights(fileId);
        return ResponseEntity.ok(ApiResponse.success("Insights generated", insights));
    }

    @GetMapping("/files/{fileId}/insights")
    public ResponseEntity<ApiResponse<DocumentInsightEntity>> getInsights(@PathVariable("fileId") UUID fileId) {
        DocumentInsightEntity insights = insightService.getInsights(fileId);
        return ResponseEntity.ok(ApiResponse.success("Insights retrieved", insights));
    }
}
