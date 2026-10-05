package com.sohel.cloudstorage.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.entity.DocumentInsightEntity;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.repository.DocumentInsightRepository;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.service.InsightService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {

    private final DocumentInsightRepository insightRepository;
    private final FileRepository fileRepository;

    @Override
    @Transactional
    public DocumentInsightEntity generateInsights(UUID fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        if (insightRepository.findByFile(file).isPresent()) {
            return insightRepository.findByFile(file).get();
        }

        log.info("Simulating AI insight generation for file {}", fileId);
        
        DocumentInsightEntity insight = DocumentInsightEntity.builder()
                .file(file)
                .summary("This document provides a comprehensive overview...")
                .keywords("finance, report, Q3, MRR")
                .topics("Finance, Business")
                .entities("MegaCorp, John Doe")
                .language("en")
                .estimatedReadingTime(5)
                .documentType("Financial Report")
                .sentiment("Positive")
                .complexity("Advanced")
                .build();
                
        return insightRepository.save(insight);
    }

    @Override
    public DocumentInsightEntity getInsights(UUID fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        return insightRepository.findByFile(file)
                .orElseThrow(() -> new ResourceNotFoundException("Insights not found for file"));
    }
}
