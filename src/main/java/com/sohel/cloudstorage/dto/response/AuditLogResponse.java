package com.sohel.cloudstorage.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private UUID id;
    private LocalDateTime timestamp;
    private String userEmail;
    private String ipAddress;
    private String device;
    private String browser;
    private String action;
    private String targetResource;
    private String targetResourceId;
    private String workspaceName;
    private String status;
}
