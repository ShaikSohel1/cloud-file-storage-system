package com.sohel.cloudstorage.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.TrashItemEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface TrashItemRepository extends JpaRepository<TrashItemEntity, UUID> {
    List<TrashItemEntity> findByDeletedByOrderByDeletedAtDesc(UserEntity deletedBy);
    List<TrashItemEntity> findByExpirationDateBefore(LocalDateTime date);
}
