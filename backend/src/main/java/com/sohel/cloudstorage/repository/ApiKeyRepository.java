package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.ApiKeyEntity;
import com.sohel.cloudstorage.entity.OrganizationEntity;

public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, UUID> {
    List<ApiKeyEntity> findByOrganization(OrganizationEntity organization);
    Optional<ApiKeyEntity> findByKeyValue(String keyValue);
}
