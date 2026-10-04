package com.sohel.cloudstorage.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.response.FavoriteResponse;
import com.sohel.cloudstorage.entity.FavoriteEntity;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FolderEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.entity.WorkspaceEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.Phase5Mapper;
import com.sohel.cloudstorage.repository.FavoriteRepository;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.repository.FolderRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.repository.WorkspaceRepository;
import com.sohel.cloudstorage.service.FavoriteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final WorkspaceRepository workspaceRepository;
    private final Phase5Mapper phase5Mapper;

    @Override
    public List<FavoriteResponse> getUserFavorites(String username) {
        UserEntity user = getUser(username);
        return favoriteRepository.findByUser(user).stream()
                .map(phase5Mapper::toFavoriteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FavoriteResponse addFavorite(String username, UUID fileId, UUID folderId, UUID workspaceId) {
        UserEntity user = getUser(username);
        
        FileEntity file = null;
        FolderEntity folder = null;
        WorkspaceEntity workspace = null;

        if (fileId != null) {
            file = fileRepository.findById(fileId).orElseThrow(() -> new ResourceNotFoundException("File not found"));
            if (favoriteRepository.findByUserAndFile(user, file).isPresent()) {
                throw new IllegalArgumentException("File already in favorites");
            }
            file.setFavorite(true);
        } else if (folderId != null) {
            folder = folderRepository.findById(folderId).orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
            if (favoriteRepository.findByUserAndFolder(user, folder).isPresent()) {
                throw new IllegalArgumentException("Folder already in favorites");
            }
            folder.setFavorite(true);
        } else if (workspaceId != null) {
            workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
            if (favoriteRepository.findByUserAndWorkspace(user, workspace).isPresent()) {
                throw new IllegalArgumentException("Workspace already in favorites");
            }
        } else {
            throw new IllegalArgumentException("Must provide fileId, folderId, or workspaceId");
        }

        FavoriteEntity favorite = FavoriteEntity.builder()
                .user(user)
                .file(file)
                .folder(folder)
                .workspace(workspace)
                .build();
                
        favorite = favoriteRepository.save(favorite);
        return phase5Mapper.toFavoriteResponse(favorite);
    }

    @Override
    @Transactional
    public String removeFavorite(String username, UUID favoriteId) {
        UserEntity user = getUser(username);
        FavoriteEntity favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));

        if (!favorite.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized to remove this favorite");
        }
        
        if (favorite.getFile() != null) {
            favorite.getFile().setFavorite(false);
            fileRepository.save(favorite.getFile());
        }
        if (favorite.getFolder() != null) {
            favorite.getFolder().setFavorite(false);
            folderRepository.save(favorite.getFolder());
        }

        favoriteRepository.delete(favorite);
        return "Favorite removed";
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
