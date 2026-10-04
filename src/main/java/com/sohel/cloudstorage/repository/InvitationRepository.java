package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sohel.cloudstorage.entity.InvitationEntity;
import com.sohel.cloudstorage.enums.InvitationStatus;

public interface InvitationRepository extends JpaRepository<InvitationEntity, UUID> {
    Optional<InvitationEntity> findByToken(String token);
    List<InvitationEntity> findByEmailAndStatus(String email, InvitationStatus status);
}
