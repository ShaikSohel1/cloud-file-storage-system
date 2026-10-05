package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.OrganizationEntity;
import com.sohel.cloudstorage.entity.OrganizationMemberEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMemberEntity, UUID> {
    List<OrganizationMemberEntity> findByOrganization(OrganizationEntity organization);
    List<OrganizationMemberEntity> findByUser(UserEntity user);
    Optional<OrganizationMemberEntity> findByOrganizationAndUser(OrganizationEntity organization, UserEntity user);
}
