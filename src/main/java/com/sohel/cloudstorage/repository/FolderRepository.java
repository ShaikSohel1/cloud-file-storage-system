package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.FolderEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface FolderRepository extends JpaRepository<FolderEntity, UUID> {
    List<FolderEntity> findByOwnerAndParentFolderAndDeletedFalse(UserEntity owner, FolderEntity parentFolder);
    List<FolderEntity> findByOwnerAndParentFolderIsNullAndDeletedFalse(UserEntity owner);
    List<FolderEntity> findByOwnerAndFavoriteTrueAndDeletedFalse(UserEntity owner);
    List<FolderEntity> findByOwnerAndDeletedTrue(UserEntity owner);
    Optional<FolderEntity> findByIdAndOwner(UUID id, UserEntity owner);
    List<FolderEntity> findByNameContainingIgnoreCaseAndOwnerAndDeletedFalse(String name, UserEntity owner);
    long countByOwnerAndDeletedFalse(UserEntity owner);
}
