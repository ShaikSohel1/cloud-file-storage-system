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
public class SecurityLogResponse {
    private UUID id;
    private LocalDateTime timestamp;
    private String userEmail;
    private String usernameAttempt;
    private String eventType;
    private String ipAddress;
    private String device;
    private String details;
}
