package com.sohel.cloudstorage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long storageUsed;
    private long storageRemaining;
    private long totalFiles;
    private long totalFolders;
    private long totalWorkspaces;
    private long totalSharedFiles;
    private long totalPublicLinks;
}
