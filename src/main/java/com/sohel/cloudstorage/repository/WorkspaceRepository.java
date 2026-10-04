package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.entity.WorkspaceEntity;

public interface WorkspaceRepository extends JpaRepository<WorkspaceEntity, UUID> {
    List<WorkspaceEntity> findByOwner(UserEntity owner);
    Optional<WorkspaceEntity> findByIdAndOwner(UUID id, UserEntity owner);
}
