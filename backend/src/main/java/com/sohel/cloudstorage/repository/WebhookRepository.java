package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.OrganizationEntity;
import com.sohel.cloudstorage.entity.WebhookEntity;

public interface WebhookRepository extends JpaRepository<WebhookEntity, UUID> {
    List<WebhookEntity> findByOrganization(OrganizationEntity organization);
    List<WebhookEntity> findByOrganizationAndActiveTrue(OrganizationEntity organization);
}
