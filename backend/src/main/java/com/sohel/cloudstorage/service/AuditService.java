package com.sohel.cloudstorage.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import com.sohel.cloudstorage.dto.response.AuditLogResponse;

public interface AuditService {
    void logAction(String username, String action, String targetResource, String targetResourceId, UUID workspaceId, String status, String ipAddress, String device, String browser);
    Page<AuditLogResponse> getAuditLogs(int page, int size);
}
