package com.sohel.cloudstorage.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.service.AnalyticsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final FileRepository fileRepository;

    @Override
    public Map<String, Object> getStorageGrowth() {
        // In a real scenario, this would query aggregated time-series data
        Map<String, Object> growth = new HashMap<>();
        growth.put("Jan", 1024000);
        growth.put("Feb", 2048000);
        growth.put("Mar", 4096000);
        return growth;
    }

    @Override
    public Map<String, Object> getFileTypesDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("pdf", 150);
        distribution.put("jpg", 300);
        distribution.put("png", 120);
        distribution.put("docx", 80);
        return distribution;
    }
}
