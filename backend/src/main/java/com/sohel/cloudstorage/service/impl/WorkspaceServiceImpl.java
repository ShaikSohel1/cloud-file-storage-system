package com.sohel.cloudstorage.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.request.WorkspaceRequest;
import com.sohel.cloudstorage.dto.response.WorkspaceResponse;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.entity.WorkspaceEntity;
import com.sohel.cloudstorage.entity.WorkspaceMemberEntity;
import com.sohel.cloudstorage.enums.PermissionRole;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.WorkspaceMapper;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.repository.WorkspaceMemberRepository;
import com.sohel.cloudstorage.repository.WorkspaceRepository;
import com.sohel.cloudstorage.service.WorkspaceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceMapper workspaceMapper;

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(String username, WorkspaceRequest request) {
        UserEntity owner = getUser(username);

        WorkspaceEntity workspace = WorkspaceEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();
        workspace = workspaceRepository.save(workspace);

        WorkspaceMemberEntity ownerMember = WorkspaceMemberEntity.builder()
                .workspace(workspace)
                .user(owner)
                .role(PermissionRole.OWNER)
                .build();
        workspaceMemberRepository.save(ownerMember);

        log.info("Workspace created: {} by {}", workspace.getName(), username);
        return enrichWorkspaceResponse(workspace);
    }

    @Override
    public WorkspaceResponse getWorkspace(String username, UUID workspaceId) {
        UserEntity user = getUser(username);
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        boolean isMember = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user).isPresent();
        if (!isMember && !workspace.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied to workspace");
        }

        return enrichWorkspaceResponse(workspace);
    }

    @Override
    public List<WorkspaceResponse> getUserWorkspaces(String username) {
        UserEntity user = getUser(username);
        List<WorkspaceMemberEntity> memberships = workspaceMemberRepository.findByUser(user);

        return memberships.stream()
                .map(m -> enrichWorkspaceResponse(m.getWorkspace()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkspaceResponse addMember(String username, UUID workspaceId, String memberEmail, PermissionRole role) {
        UserEntity requester = getUser(username);
        WorkspaceEntity workspace = workspaceRepository.findByIdAndOwner(workspaceId, requester)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found or unauthorized"));

        UserEntity newMember = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + memberEmail));

        if (workspaceMemberRepository.findByWorkspaceAndUser(workspace, newMember).isPresent()) {
            throw new IllegalArgumentException("User is already a workspace member");
        }

        WorkspaceMemberEntity member = WorkspaceMemberEntity.builder()
                .workspace(workspace)
                .user(newMember)
                .role(role != null ? role : PermissionRole.VIEWER)
                .build();
        workspaceMemberRepository.save(member);

        log.info("Added member {} to workspace {}", memberEmail, workspace.getName());
        return enrichWorkspaceResponse(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse removeMember(String username, UUID workspaceId, String memberEmail) {
        UserEntity requester = getUser(username);
        WorkspaceEntity workspace = workspaceRepository.findByIdAndOwner(workspaceId, requester)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found or unauthorized"));

        UserEntity memberUser = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + memberEmail));

        WorkspaceMemberEntity member = workspaceMemberRepository.findByWorkspaceAndUser(workspace, memberUser)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in workspace"));

        workspaceMemberRepository.delete(member);
        log.info("Removed member {} from workspace {}", memberEmail, workspace.getName());
        return enrichWorkspaceResponse(workspace);
    }

    @Override
    @Transactional
    public String deleteWorkspace(String username, UUID workspaceId) {
        UserEntity owner = getUser(username);
        WorkspaceEntity workspace = workspaceRepository.findByIdAndOwner(workspaceId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found or unauthorized"));

        workspaceMemberRepository.deleteAll(workspaceMemberRepository.findByWorkspace(workspace));
        workspaceRepository.delete(workspace);
        log.info("Deleted workspace {} by {}", workspaceId, username);
        return "Workspace deleted successfully";
    }

    private WorkspaceResponse enrichWorkspaceResponse(WorkspaceEntity workspace) {
        WorkspaceResponse response = workspaceMapper.toResponse(workspace);
        List<WorkspaceMemberEntity> members = workspaceMemberRepository.findByWorkspace(workspace);
        response.setMembers(members.stream().map(workspaceMapper::toMemberResponse).collect(Collectors.toList()));
        return response;
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
