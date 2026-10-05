package com.sohel.cloudstorage.service;

import java.util.List;
import java.util.UUID;

import com.sohel.cloudstorage.dto.request.CreateFolderRequest;
import com.sohel.cloudstorage.dto.request.UpdateFolderRequest;
import com.sohel.cloudstorage.dto.response.FolderResponse;

public interface FolderService {
    FolderResponse createFolder(String username, CreateFolderRequest request);
    FolderResponse getFolder(String username, UUID folderId);
    List<FolderResponse> getFolders(String username, UUID parentFolderId);
    FolderResponse updateFolder(String username, UUID folderId, UpdateFolderRequest request);
    FolderResponse toggleFavorite(String username, UUID folderId);
    String deleteFolder(String username, UUID folderId);
    String moveFolder(String username, UUID folderId, UUID targetFolderId);
    List<FolderResponse> getFavoriteFolders(String username);
}
