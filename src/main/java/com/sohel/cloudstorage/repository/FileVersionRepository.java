package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FileVersionEntity;

public interface FileVersionRepository extends JpaRepository<FileVersionEntity, UUID> {
    List<FileVersionEntity> findByFileOrderByVersionNumberDesc(FileEntity file);
    Optional<FileVersionEntity> findByFileAndVersionNumber(FileEntity file, Integer versionNumber);
}
