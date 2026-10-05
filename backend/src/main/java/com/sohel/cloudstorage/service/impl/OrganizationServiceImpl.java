package com.sohel.cloudstorage.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.entity.OrganizationEntity;
import com.sohel.cloudstorage.entity.OrganizationMemberEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.repository.OrganizationMemberRepository;
import com.sohel.cloudstorage.repository.OrganizationRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.OrganizationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrganizationEntity createOrganization(String name, String ownerUsername) {
        UserEntity owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OrganizationEntity org = OrganizationEntity.builder()
                .name(name)
                .build();
        org = organizationRepository.save(org);

        OrganizationMemberEntity member = OrganizationMemberEntity.builder()
                .organization(org)
                .user(owner)
                .role("OWNER")
                .build();
        organizationMemberRepository.save(member);

        owner.setCurrentOrganization(org);
        userRepository.save(owner);

        return org;
    }

    @Override
    public OrganizationEntity getOrganization(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    @Override
    public List<OrganizationEntity> getUserOrganizations(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return organizationMemberRepository.findByUser(user).stream()
                .map(OrganizationMemberEntity::getOrganization)
                .collect(Collectors.toList());
    }
}
