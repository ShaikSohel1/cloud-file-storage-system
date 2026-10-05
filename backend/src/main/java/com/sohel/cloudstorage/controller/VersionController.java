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
import com.sohel.cloudstorage.dto.response.FileVersionResponse;
import com.sohel.cloudstorage.service.VersionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/files/{fileId}/versions")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FileVersionResponse>>> getFileVersions(
            Principal principal,
            @PathVariable("fileId") UUID fileId) {
        List<FileVersionResponse> versions = versionService.getFileVersions(principal.getName(), fileId);
        return ResponseEntity.ok(ApiResponse.success("Versions retrieved", versions));
    }

    @PostMapping("/{versionNumber}/restore")
    public ResponseEntity<ApiResponse<String>> restoreVersion(
            Principal principal,
            @PathVariable("fileId") UUID fileId,
            @PathVariable("versionNumber") Integer versionNumber) {
        String result = versionService.restoreVersion(principal.getName(), fileId, versionNumber);
        return ResponseEntity.ok(ApiResponse.success("Version restored", result));
    }

    @DeleteMapping("/{versionId}")
    public ResponseEntity<ApiResponse<String>> deleteVersion(
            Principal principal,
            @PathVariable("fileId") UUID fileId,
            @PathVariable("versionId") UUID versionId) {
        String result = versionService.deleteVersion(principal.getName(), fileId, versionId);
        return ResponseEntity.ok(ApiResponse.success("Version deleted", result));
    }
}
