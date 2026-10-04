package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FolderEntity;
import com.sohel.cloudstorage.entity.SharedResourceEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface SharedResourceRepository extends JpaRepository<SharedResourceEntity, UUID> {
    List<SharedResourceEntity> findBySharedWith(UserEntity sharedWith);
    List<SharedResourceEntity> findBySharedBy(UserEntity sharedBy);
    Optional<SharedResourceEntity> findByFileAndSharedWith(FileEntity file, UserEntity sharedWith);
    Optional<SharedResourceEntity> findByFolderAndSharedWith(FolderEntity folder, UserEntity sharedWith);
    void deleteByFileAndSharedWith(FileEntity file, UserEntity sharedWith);
    void deleteByFolderAndSharedWith(FolderEntity folder, UserEntity sharedWith);
}
