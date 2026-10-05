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
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.request.TagRequest;
import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.dto.response.TagResponse;
import com.sohel.cloudstorage.service.TagService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            Principal principal,
            @Valid @RequestBody TagRequest request) {
        TagResponse response = tagService.createTag(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Tag created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getUserTags(Principal principal) {
        List<TagResponse> response = tagService.getUserTags(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved", response));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponse<String>> deleteTag(Principal principal, @PathVariable("tagId") UUID tagId) {
        String result = tagService.deleteTag(principal.getName(), tagId);
        return ResponseEntity.ok(ApiResponse.success("Tag deleted", result));
    }

    @PostMapping("/files/{fileId}/{tagId}")
    public ResponseEntity<ApiResponse<String>> addTagToFile(
            Principal principal,
            @PathVariable("fileId") UUID fileId,
            @PathVariable("tagId") UUID tagId) {
        String result = tagService.addTagToFile(principal.getName(), fileId, tagId);
        return ResponseEntity.ok(ApiResponse.success("Tag added to file", result));
    }

    @DeleteMapping("/files/{fileId}/{tagId}")
    public ResponseEntity<ApiResponse<String>> removeTagFromFile(
            Principal principal,
            @PathVariable("fileId") UUID fileId,
            @PathVariable("tagId") UUID tagId) {
        String result = tagService.removeTagFromFile(principal.getName(), fileId, tagId);
        return ResponseEntity.ok(ApiResponse.success("Tag removed from file", result));
    }
}
