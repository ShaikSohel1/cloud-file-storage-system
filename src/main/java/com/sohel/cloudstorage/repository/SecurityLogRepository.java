package com.sohel.cloudstorage.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.SecurityLogEntity;

public interface SecurityLogRepository extends JpaRepository<SecurityLogEntity, UUID> {
    Page<SecurityLogEntity> findAllByOrderByTimestampDesc(Pageable pageable);
}
