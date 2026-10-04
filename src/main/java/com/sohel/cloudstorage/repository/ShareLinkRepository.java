package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FolderEntity;
import com.sohel.cloudstorage.entity.ShareLinkEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface ShareLinkRepository extends JpaRepository<ShareLinkEntity, UUID> {
    Optional<ShareLinkEntity> findByToken(String token);
    List<ShareLinkEntity> findByCreatedBy(UserEntity createdBy);
    Optional<ShareLinkEntity> findByFileAndCreatedBy(FileEntity file, UserEntity createdBy);
    Optional<ShareLinkEntity> findByFolderAndCreatedBy(FolderEntity folder, UserEntity createdBy);
}
