package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FileTagEntity;
import com.sohel.cloudstorage.entity.TagEntity;

public interface FileTagRepository extends JpaRepository<FileTagEntity, UUID> {
    List<FileTagEntity> findByFile(FileEntity file);
    List<FileTagEntity> findByTag(TagEntity tag);
    Optional<FileTagEntity> findByFileAndTag(FileEntity file, TagEntity tag);
}
