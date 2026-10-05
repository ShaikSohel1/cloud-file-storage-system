package com.sohel.cloudstorage.service;

import java.util.List;
import java.util.UUID;

import com.sohel.cloudstorage.entity.OrganizationEntity;

public interface OrganizationService {
    OrganizationEntity createOrganization(String name, String ownerUsername);
    OrganizationEntity getOrganization(UUID id);
    List<OrganizationEntity> getUserOrganizations(String username);
}
