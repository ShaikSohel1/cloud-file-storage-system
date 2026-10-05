package com.sohel.cloudstorage.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.request.CreateFolderRequest;
import com.sohel.cloudstorage.dto.request.MoveItemRequest;
import com.sohel.cloudstorage.dto.request.UpdateFolderRequest;
import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.dto.response.FolderResponse;
import com.sohel.cloudstorage.service.FolderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            Principal principal,
            @Valid @RequestBody CreateFolderRequest request) {
        FolderResponse response = folderService.createFolder(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Folder created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> getFolder(
            Principal principal,
            @PathVariable("id") UUID folderId) {
        FolderResponse response = folderService.getFolder(principal.getName(), folderId);
        return ResponseEntity.ok(ApiResponse.success("Folder retrieved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getFolders(
            Principal principal,
            @RequestParam(name = "parentId", required = false) UUID parentFolderId) {
        List<FolderResponse> response = folderService.getFolders(principal.getName(), parentFolderId);
        return ResponseEntity.ok(ApiResponse.success("Folders retrieved", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> updateFolder(
            Principal principal,
            @PathVariable("id") UUID folderId,
            @Valid @RequestBody UpdateFolderRequest request) {
        FolderResponse response = folderService.updateFolder(principal.getName(), folderId, request);
        return ResponseEntity.ok(ApiResponse.success("Folder updated", response));
    }

    @PutMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<FolderResponse>> toggleFavorite(
            Principal principal,
            @PathVariable("id") UUID folderId) {
        FolderResponse response = folderService.toggleFavorite(principal.getName(), folderId);
        return ResponseEntity.ok(ApiResponse.success("Folder favorite updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteFolder(
            Principal principal,
            @PathVariable("id") UUID folderId) {
        String message = folderService.deleteFolder(principal.getName(), folderId);
        return ResponseEntity.ok(ApiResponse.success("Folder deleted", message));
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<ApiResponse<String>> moveFolder(
            Principal principal,
            @PathVariable("id") UUID folderId,
            @RequestBody MoveItemRequest request) {
        String message = folderService.moveFolder(principal.getName(), folderId, request.getTargetFolderId());
        return ResponseEntity.ok(ApiResponse.success("Folder moved", message));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getFavoriteFolders(Principal principal) {
        List<FolderResponse> response = folderService.getFavoriteFolders(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Favorite folders retrieved", response));
    }
}
