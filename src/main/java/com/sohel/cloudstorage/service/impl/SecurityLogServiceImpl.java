package com.sohel.cloudstorage.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.response.SecurityLogResponse;
import com.sohel.cloudstorage.entity.SecurityLogEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.mapper.Phase5Mapper;
import com.sohel.cloudstorage.repository.SecurityLogRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.SecurityLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecurityLogServiceImpl implements SecurityLogService {

    private final SecurityLogRepository securityLogRepository;
    private final UserRepository userRepository;
    private final Phase5Mapper phase5Mapper;

    @Override
    @Async
    @Transactional
    public void logSecurityEvent(String usernameAttempt, String eventType, String ipAddress, String device, String details) {
        UserEntity user = null;
        if (usernameAttempt != null) {
            user = userRepository.findByUsername(usernameAttempt).orElse(null);
            if (user == null) {
                user = userRepository.findByEmail(usernameAttempt).orElse(null);
            }
        }

        SecurityLogEntity log = SecurityLogEntity.builder()
                .user(user)
                .usernameAttempt(usernameAttempt)
                .eventType(eventType)
                .ipAddress(ipAddress)
                .device(device)
                .details(details)
                .build();
                
        securityLogRepository.save(log);
    }

    @Override
    public Page<SecurityLogResponse> getSecurityLogs(int page, int size) {
        return securityLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size))
                .map(phase5Mapper::toSecurityLogResponse);
    }
}
