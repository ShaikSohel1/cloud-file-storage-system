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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.dto.response.FavoriteResponse;
import com.sohel.cloudstorage.service.FavoriteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getUserFavorites(Principal principal) {
        List<FavoriteResponse> favorites = favoriteService.getUserFavorites(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Favorites retrieved", favorites));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(
            Principal principal,
            @RequestParam(name = "fileId", required = false) UUID fileId,
            @RequestParam(name = "folderId", required = false) UUID folderId,
            @RequestParam(name = "workspaceId", required = false) UUID workspaceId) {
        FavoriteResponse response = favoriteService.addFavorite(principal.getName(), fileId, folderId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Favorite added", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> removeFavorite(
            Principal principal,
            @PathVariable("id") UUID favoriteId) {
        String result = favoriteService.removeFavorite(principal.getName(), favoriteId);
        return ResponseEntity.ok(ApiResponse.success("Favorite removed", result));
    }
}
