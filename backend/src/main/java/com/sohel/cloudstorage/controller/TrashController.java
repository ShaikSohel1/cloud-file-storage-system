package com.sohel.cloudstorage.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.dto.response.FileResponse;
import com.sohel.cloudstorage.dto.response.FolderResponse;
import com.sohel.cloudstorage.service.TrashService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/trash")
@RequiredArgsConstructor
public class TrashController {

    private final TrashService trashService;

    @GetMapping("/files")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getTrashedFiles(Principal principal) {
        List<FileResponse> files = trashService.getTrashedFiles(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Trashed files retrieved", files));
    }

    @GetMapping("/folders")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getTrashedFolders(Principal principal) {
        List<FolderResponse> folders = trashService.getTrashedFolders(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Trashed folders retrieved", folders));
    }

    @PostMapping("/files/{id}/restore")
    public ResponseEntity<ApiResponse<String>> restoreFile(Principal principal, @PathVariable("id") UUID fileId) {
        String result = trashService.restoreFile(principal.getName(), fileId);
        return ResponseEntity.ok(ApiResponse.success("File restored", result));
    }

    @PostMapping("/folders/{id}/restore")
    public ResponseEntity<ApiResponse<String>> restoreFolder(Principal principal, @PathVariable("id") UUID folderId) {
        String result = trashService.restoreFolder(principal.getName(), folderId);
        return ResponseEntity.ok(ApiResponse.success("Folder restored", result));
    }

    @DeleteMapping("/files/{id}")
    public ResponseEntity<ApiResponse<String>> permanentlyDeleteFile(Principal principal, @PathVariable("id") UUID fileId) {
        String result = trashService.permanentlyDeleteFile(principal.getName(), fileId);
        return ResponseEntity.ok(ApiResponse.success("File permanently deleted", result));
    }

    @DeleteMapping("/folders/{id}")
    public ResponseEntity<ApiResponse<String>> permanentlyDeleteFolder(Principal principal, @PathVariable("id") UUID folderId) {
        String result = trashService.permanentlyDeleteFolder(principal.getName(), folderId);
        return ResponseEntity.ok(ApiResponse.success("Folder permanently deleted", result));
    }

    @DeleteMapping("/empty")
    public ResponseEntity<ApiResponse<String>> emptyTrash(Principal principal) {
        String result = trashService.emptyTrash(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Trash emptied", result));
    }
}
