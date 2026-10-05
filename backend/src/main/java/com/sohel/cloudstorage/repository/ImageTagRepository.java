package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.ImageTagEntity;

public interface ImageTagRepository extends JpaRepository<ImageTagEntity, UUID> {
    List<ImageTagEntity> findByFile(FileEntity file);
}
