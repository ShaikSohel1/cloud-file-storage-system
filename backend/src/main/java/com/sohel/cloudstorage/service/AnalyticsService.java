package com.sohel.cloudstorage.service;

import java.util.Map;

public interface AnalyticsService {
    Map<String, Object> getStorageGrowth();
    Map<String, Object> getFileTypesDistribution();
}
