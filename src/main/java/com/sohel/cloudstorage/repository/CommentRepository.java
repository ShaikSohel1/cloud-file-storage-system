package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.CommentEntity;
import com.sohel.cloudstorage.entity.FileEntity;

public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {
    List<CommentEntity> findByFileOrderByCreatedAtAsc(FileEntity file);
}
