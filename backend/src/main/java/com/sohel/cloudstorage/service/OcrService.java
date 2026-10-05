package com.sohel.cloudstorage.service;

import java.util.UUID;

public interface OcrService {
    String extractTextFromImage(UUID fileId);
}
