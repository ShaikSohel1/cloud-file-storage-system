package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sohel.cloudstorage.entity.FavoriteEntity;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FolderEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.entity.WorkspaceEntity;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, UUID> {
    List<FavoriteEntity> findByUser(UserEntity user);
    Optional<FavoriteEntity> findByUserAndFile(UserEntity user, FileEntity file);
    Optional<FavoriteEntity> findByUserAndFolder(UserEntity user, FolderEntity folder);
    Optional<FavoriteEntity> findByUserAndWorkspace(UserEntity user, WorkspaceEntity workspace);
}
