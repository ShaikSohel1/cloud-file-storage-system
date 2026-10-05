package com.sohel.cloudstorage.service;

import java.util.List;
import java.util.UUID;

import com.sohel.cloudstorage.dto.response.FavoriteResponse;

public interface FavoriteService {
    List<FavoriteResponse> getUserFavorites(String username);
    FavoriteResponse addFavorite(String username, UUID fileId, UUID folderId, UUID workspaceId);
    String removeFavorite(String username, UUID favoriteId);
}
