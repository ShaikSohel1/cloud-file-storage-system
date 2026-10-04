package com.sohel.cloudstorage.service;

import java.util.UUID;

public interface AiService {
    String summarizeDocument(UUID fileId);
    String extractActionItems(UUID fileId);
    String generateChatResponse(UUID fileId, String prompt);
}
