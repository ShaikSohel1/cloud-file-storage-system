package com.sohel.cloudstorage.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.response.AuditLogResponse;
import com.sohel.cloudstorage.entity.AuditLogEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.entity.WorkspaceEntity;
import com.sohel.cloudstorage.mapper.Phase5Mapper;
import com.sohel.cloudstorage.repository.AuditLogRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.repository.WorkspaceRepository;
import com.sohel.cloudstorage.service.AuditService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final Phase5Mapper phase5Mapper;

    @Override
    @Async
    @Transactional
    public void logAction(String username, String action, String targetResource, String targetResourceId, UUID workspaceId, String status, String ipAddress, String device, String browser) {
        UserEntity user = null;
        if (username != null) {
            user = userRepository.findByUsername(username).orElse(null);
        }
        
        WorkspaceEntity workspace = null;
        if (workspaceId != null) {
            workspace = workspaceRepository.findById(workspaceId).orElse(null);
        }

        AuditLogEntity log = AuditLogEntity.builder()
                .user(user)
                .action(action)
                .targetResource(targetResource)
                .targetResourceId(targetResourceId)
                .workspace(workspace)
                .status(status)
                .ipAddress(ipAddress)
                .device(device)
                .browser(browser)
                .build();
                
        auditLogRepository.save(log);
    }

    @Override
    public Page<AuditLogResponse> getAuditLogs(int page, int size) {
        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size))
                .map(phase5Mapper::toAuditLogResponse);
    }
}
