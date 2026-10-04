package com.sohel.cloudstorage.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.request.WorkspaceRequest;
import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.dto.response.WorkspaceResponse;
import com.sohel.cloudstorage.enums.PermissionRole;
import com.sohel.cloudstorage.service.WorkspaceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            Principal principal,
            @Valid @RequestBody WorkspaceRequest request) {
        WorkspaceResponse response = workspaceService.createWorkspace(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Workspace created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspace(
            Principal principal,
            @PathVariable("id") UUID workspaceId) {
        WorkspaceResponse response = workspaceService.getWorkspace(principal.getName(), workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Workspace retrieved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getUserWorkspaces(Principal principal) {
        List<WorkspaceResponse> response = workspaceService.getUserWorkspaces(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Workspaces retrieved", response));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> addMember(
            Principal principal,
            @PathVariable("id") UUID workspaceId,
            @RequestParam("email") String memberEmail,
            @RequestParam(name = "role", required = false) PermissionRole role) {
        WorkspaceResponse response = workspaceService.addMember(principal.getName(), workspaceId, memberEmail, role);
        return ResponseEntity.ok(ApiResponse.success("Member added to workspace", response));
    }

    @DeleteMapping("/{id}/members")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> removeMember(
            Principal principal,
            @PathVariable("id") UUID workspaceId,
            @RequestParam("email") String memberEmail) {
        WorkspaceResponse response = workspaceService.removeMember(principal.getName(), workspaceId, memberEmail);
        return ResponseEntity.ok(ApiResponse.success("Member removed from workspace", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteWorkspace(Principal principal, @PathVariable("id") UUID workspaceId) {
        String message = workspaceService.deleteWorkspace(principal.getName(), workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted", message));
    }
}
