package com.sohel.cloudstorage.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.response.InvitationResponse;
import com.sohel.cloudstorage.entity.InvitationEntity;
import com.sohel.cloudstorage.entity.SharedResourceEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.entity.WorkspaceMemberEntity;
import com.sohel.cloudstorage.enums.InvitationStatus;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.ShareMapper;
import com.sohel.cloudstorage.repository.InvitationRepository;
import com.sohel.cloudstorage.repository.SharedResourceRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.repository.WorkspaceMemberRepository;
import com.sohel.cloudstorage.service.InvitationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final SharedResourceRepository sharedResourceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ShareMapper shareMapper;

    @Override
    public List<InvitationResponse> getMyPendingInvitations(String username) {
        UserEntity user = getUser(username);
        return invitationRepository.findByEmailAndStatus(user.getEmail(), InvitationStatus.PENDING).stream()
                .map(shareMapper::toInvitationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String acceptInvitation(String username, UUID invitationId) {
        UserEntity user = getUser(username);
        InvitationEntity invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Unauthorized to accept this invitation");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        if (invitation.getWorkspace() != null) {
            WorkspaceMemberEntity member = WorkspaceMemberEntity.builder()
                    .workspace(invitation.getWorkspace())
                    .user(user)
                    .role(invitation.getRole())
                    .build();
            workspaceMemberRepository.save(member);
        } else if (invitation.getFile() != null || invitation.getFolder() != null) {
            SharedResourceEntity resource = SharedResourceEntity.builder()
                    .file(invitation.getFile())
                    .folder(invitation.getFolder())
                    .sharedWith(user)
                    .sharedBy(invitation.getInvitedBy())
                    .role(invitation.getRole())
                    .build();
            sharedResourceRepository.save(resource);
        }

        return "Invitation accepted successfully";
    }

    @Override
    @Transactional
    public String declineInvitation(String username, UUID invitationId) {
        UserEntity user = getUser(username);
        InvitationEntity invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Unauthorized to decline this invitation");
        }

        invitation.setStatus(InvitationStatus.DECLINED);
        invitationRepository.save(invitation);

        return "Invitation declined";
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
