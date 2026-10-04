package com.sohel.cloudstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.AdminSettingsEntity;

public interface AdminSettingsRepository extends JpaRepository<AdminSettingsEntity, String> {
}
