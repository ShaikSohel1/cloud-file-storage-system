package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.FileActivityEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface FileActivityRepository extends JpaRepository<FileActivityEntity, UUID> {
    List<FileActivityEntity> findByUserOrderByTimestampDesc(UserEntity user, Pageable pageable);
}
