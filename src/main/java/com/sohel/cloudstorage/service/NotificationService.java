package com.sohel.cloudstorage.service;

import java.util.List;
import java.util.UUID;

import com.sohel.cloudstorage.dto.response.NotificationResponse;

public interface NotificationService {
    List<NotificationResponse> getNotifications(String username);
    long getUnreadCount(String username);
    String markAsRead(String username, UUID notificationId);
}
