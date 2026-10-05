package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.OrganizationEntity;
import com.sohel.cloudstorage.entity.TeamEntity;

public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {
    List<TeamEntity> findByOrganization(OrganizationEntity organization);
}
