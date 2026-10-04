package com.sohel.cloudstorage.service;

import com.sohel.cloudstorage.dto.response.StorageStatsResponse;

public interface StatisticsService {
    StorageStatsResponse getStorageStats(String username);
}
