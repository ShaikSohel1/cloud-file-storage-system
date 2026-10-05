package com.sohel.cloudstorage.dto.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageStatsResponse {
    private Long storageUsed;
    private Long storageLimit;
    private Double usagePercentage;
    private long totalFiles;
    private long totalFolders;
    private Map<String, Long> sizeByCategory; // Images, Documents, Videos, Audio, Code, Archives, Other
}
