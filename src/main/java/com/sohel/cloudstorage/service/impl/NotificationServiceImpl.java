package com.sohel.cloudstorage.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.response.NotificationResponse;
import com.sohel.cloudstorage.entity.NotificationEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.ActivityMapper;
import com.sohel.cloudstorage.repository.NotificationRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ActivityMapper activityMapper;

    @Override
    public List<NotificationResponse> getNotifications(String username) {
        UserEntity user = getUser(username);
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(activityMapper::toNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(String username) {
        UserEntity user = getUser(username);
        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    @Override
    @Transactional
    public String markAsRead(String username, UUID notificationId) {
        UserEntity user = getUser(username);
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized to update notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return "Notification marked as read";
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
