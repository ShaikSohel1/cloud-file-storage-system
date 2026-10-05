package com.sohel.cloudstorage.service;

import java.util.List;
import java.util.UUID;

import com.sohel.cloudstorage.dto.request.WorkspaceRequest;
import com.sohel.cloudstorage.dto.response.WorkspaceResponse;
import com.sohel.cloudstorage.enums.PermissionRole;

public interface WorkspaceService {
    WorkspaceResponse createWorkspace(String username, WorkspaceRequest request);
    WorkspaceResponse getWorkspace(String username, UUID workspaceId);
    List<WorkspaceResponse> getUserWorkspaces(String username);
    WorkspaceResponse addMember(String username, UUID workspaceId, String memberEmail, PermissionRole role);
    WorkspaceResponse removeMember(String username, UUID workspaceId, String memberEmail);
    String deleteWorkspace(String username, UUID workspaceId);
}
