package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FolderEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface FileRepository extends JpaRepository<FileEntity, UUID>, JpaSpecificationExecutor<FileEntity> {
    Optional<FileEntity> findByName(String name);
    Optional<FileEntity> findByNameAndOwner(String name, UserEntity owner);
    Optional<FileEntity> findByIdAndOwner(UUID id, UserEntity owner);

    List<FileEntity> findByOwnerAndFolderAndDeletedFalse(UserEntity owner, FolderEntity folder);
    List<FileEntity> findByOwnerAndFolderIsNullAndDeletedFalse(UserEntity owner);
    List<FileEntity> findByOwnerAndFavoriteTrueAndDeletedFalse(UserEntity owner);
    List<FileEntity> findByOwnerAndDeletedTrue(UserEntity owner);

    List<FileEntity> findByNameContainingIgnoreCaseAndOwnerAndDeletedFalse(String name, UserEntity owner);

    long countByOwnerAndDeletedFalse(UserEntity owner);

    @Query("SELECT SUM(f.size) FROM FileEntity f WHERE f.owner = :owner AND f.deleted = false")
    Long sumSizeByOwnerAndDeletedFalse(@Param("owner") UserEntity owner);
}