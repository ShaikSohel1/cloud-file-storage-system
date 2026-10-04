package com.sohel.cloudstorage.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.DocumentInsightEntity;
import com.sohel.cloudstorage.entity.FileEntity;

public interface DocumentInsightRepository extends JpaRepository<DocumentInsightEntity, UUID> {
    Optional<DocumentInsightEntity> findByFile(FileEntity file);
}
