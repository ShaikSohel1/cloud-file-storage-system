package com.sohel.cloudstorage.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sohel.cloudstorage.service.OcrService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OcrServiceImpl implements OcrService {

    @Override
    public String extractTextFromImage(UUID fileId) {
        log.info("Simulating OCR processing for image file {}", fileId);
        // Simulate extraction from an invoice or receipt
        return "INVOICE #10245\nDate: 2026-08-01\nTotal Amount: $450.00\nVendor: Acme Corp\nItems: Web Hosting, Domain Registration";
    }
}
