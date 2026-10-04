package com.sohel.cloudstorage.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.dto.response.FileResponse;
import com.sohel.cloudstorage.dto.response.FolderResponse;
import com.sohel.cloudstorage.service.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/files")
    public ResponseEntity<ApiResponse<List<FileResponse>>> searchFiles(
            Principal principal,
            @RequestParam("q") String query) {
        List<FileResponse> files = searchService.searchFiles(principal.getName(), query);
        return ResponseEntity.ok(ApiResponse.success("Files matching search", files));
    }

    @GetMapping("/folders")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> searchFolders(
            Principal principal,
            @RequestParam("q") String query) {
        List<FolderResponse> folders = searchService.searchFolders(principal.getName(), query);
        return ResponseEntity.ok(ApiResponse.success("Folders matching search", folders));
    }
}
