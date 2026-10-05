package com.sohel.cloudstorage.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import com.sohel.cloudstorage.dto.request.BulkActionRequest;
import com.sohel.cloudstorage.dto.request.MoveItemRequest;
import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.dto.response.FileResponse;
import com.sohel.cloudstorage.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileResponse>> upload(
            Principal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "folderId", required = false) UUID folderId) throws IOException {
        FileResponse response = fileService.uploadFile(principal.getName(), file, folderId);
        return ResponseEntity.ok(ApiResponse.success("Upload successful", response));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(Principal principal, @PathVariable("id") UUID fileId) throws IOException {
        byte[] data = fileService.downloadFile(principal.getName(), fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file_" + fileId + "\"")
                .body(data);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> view(Principal principal, @PathVariable("id") UUID fileId) throws IOException {
        byte[] data = fileService.downloadFile(principal.getName(), fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FileResponse>>> listFiles(
            Principal principal,
            @RequestParam(name = "folderId", required = false) UUID folderId) {
        List<FileResponse> files = fileService.getFiles(principal.getName(), folderId);
        return ResponseEntity.ok(ApiResponse.success("Files retrieved successfully", files));
    }

    @PutMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<FileResponse>> toggleFavorite(Principal principal, @PathVariable("id") UUID fileId) {
        FileResponse response = fileService.toggleFavorite(principal.getName(), fileId);
        return ResponseEntity.ok(ApiResponse.success("Favorite updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> softDelete(Principal principal, @PathVariable("id") UUID fileId) {
        String result = fileService.softDeleteFile(principal.getName(), fileId);
        return ResponseEntity.ok(ApiResponse.success("Delete successful", result));
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<ApiResponse<FileResponse>> rename(
            Principal principal,
            @PathVariable("id") UUID fileId,
            @RequestParam("name") String newName) {
        FileResponse response = fileService.renameFile(principal.getName(), fileId, newName);
        return ResponseEntity.ok(ApiResponse.success("Rename successful", response));
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<ApiResponse<FileResponse>> moveFile(
            Principal principal,
            @PathVariable("id") UUID fileId,
            @RequestBody MoveItemRequest request) {
        FileResponse response = fileService.moveFile(principal.getName(), fileId, request.getTargetFolderId());
        return ResponseEntity.ok(ApiResponse.success("File moved successfully", response));
    }

    @PostMapping("/bulk/{action}")
    public ResponseEntity<ApiResponse<String>> bulkAction(
            Principal principal,
            @PathVariable("action") String action,
            @RequestBody BulkActionRequest request) {
        String result = fileService.handleBulkActions(principal.getName(), action, request);
        return ResponseEntity.ok(ApiResponse.success("Bulk action processed", result));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getFavorites(Principal principal) {
        List<FileResponse> files = fileService.getFavoriteFiles(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Favorite files retrieved", files));
    }
}