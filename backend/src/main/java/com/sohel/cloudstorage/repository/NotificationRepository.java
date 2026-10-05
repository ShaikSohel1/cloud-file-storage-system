package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.NotificationEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findByRecipientOrderByCreatedAtDesc(UserEntity recipient);
    long countByRecipientAndReadFalse(UserEntity recipient);
}
