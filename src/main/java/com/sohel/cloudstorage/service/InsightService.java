package com.sohel.cloudstorage.service;

import java.util.UUID;
import com.sohel.cloudstorage.entity.DocumentInsightEntity;

public interface InsightService {
    DocumentInsightEntity generateInsights(UUID fileId);
    DocumentInsightEntity getInsights(UUID fileId);
}
