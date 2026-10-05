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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.request.CreateShareLinkRequest;
import com.sohel.cloudstorage.dto.request.ShareResourceRequest;
import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.dto.response.FileResponse;
import com.sohel.cloudstorage.dto.response.ShareLinkResponse;
import com.sohel.cloudstorage.dto.response.SharedResourceResponse;
import com.sohel.cloudstorage.service.ShareService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping("/shares")
    public ResponseEntity<ApiResponse<SharedResourceResponse>> shareResource(
            Principal principal,
            @Valid @RequestBody ShareResourceRequest request) {
        SharedResourceResponse response = shareService.shareResource(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Resource shared successfully", response));
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<ApiResponse<List<SharedResourceResponse>>> getSharedWithMe(Principal principal) {
        List<SharedResourceResponse> response = shareService.getSharedWithMe(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Shared with me retrieved", response));
    }

    @GetMapping("/shared-by-me")
    public ResponseEntity<ApiResponse<List<SharedResourceResponse>>> getSharedByMe(Principal principal) {
        List<SharedResourceResponse> response = shareService.getSharedByMe(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Shared by me retrieved", response));
    }

    @PostMapping("/share-links")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> createShareLink(
            Principal principal,
            @Valid @RequestBody CreateShareLinkRequest request) {
        ShareLinkResponse response = shareService.createShareLink(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Share link created", response));
    }

    @GetMapping("/public/share/{token}")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> getPublicShareLink(
            @PathVariable("token") String token,
            @RequestParam(name = "password", required = false) String password) {
        ShareLinkResponse response = shareService.getShareLinkByToken(token, password);
        return ResponseEntity.ok(ApiResponse.success("Share link retrieved", response));
    }

    @GetMapping("/public/share/{token}/file")
    public ResponseEntity<ApiResponse<FileResponse>> getPublicFile(
            @PathVariable("token") String token,
            @RequestParam(name = "password", required = false) String password) {
        FileResponse response = shareService.accessPublicFile(token, password);
        return ResponseEntity.ok(ApiResponse.success("Public file retrieved", response));
    }

    @DeleteMapping("/share-links/{id}")
    public ResponseEntity<ApiResponse<String>> disableShareLink(
            Principal principal,
            @PathVariable("id") UUID linkId) {
        String message = shareService.disableShareLink(principal.getName(), linkId);
        return ResponseEntity.ok(ApiResponse.success("Share link disabled", message));
    }
}
