package com.sohel.cloudstorage.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.OrganizationEntity;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {
    Optional<OrganizationEntity> findByName(String name);
    Optional<OrganizationEntity> findByCustomDomain(String customDomain);
}
