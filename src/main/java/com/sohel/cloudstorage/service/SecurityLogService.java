package com.sohel.cloudstorage.service;

import org.springframework.data.domain.Page;
import com.sohel.cloudstorage.dto.response.SecurityLogResponse;

public interface SecurityLogService {
    void logSecurityEvent(String usernameAttempt, String eventType, String ipAddress, String device, String details);
    Page<SecurityLogResponse> getSecurityLogs(int page, int size);
}
