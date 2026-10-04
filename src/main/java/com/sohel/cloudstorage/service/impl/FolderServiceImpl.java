package com.sohel.cloudstorage.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.request.CreateFolderRequest;
import com.sohel.cloudstorage.dto.request.UpdateFolderRequest;
import com.sohel.cloudstorage.dto.response.BreadcrumbResponse;
import com.sohel.cloudstorage.dto.response.FolderResponse;
import com.sohel.cloudstorage.entity.FolderEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.FolderMapper;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.repository.FolderRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.FolderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FolderMapper folderMapper;

    @Override
    @Transactional
    public FolderResponse createFolder(String username, CreateFolderRequest request) {
        UserEntity user = getUser(username);

        FolderEntity parentFolder = null;
        if (request.getParentFolderId() != null) {
            parentFolder = folderRepository.findByIdAndOwner(request.getParentFolderId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent folder not found"));
        }

        FolderEntity folder = FolderEntity.builder()
                .name(request.getName())
                .owner(user)
                .parentFolder(parentFolder)
                .color(request.getColor() != null ? request.getColor() : "#4A90E2")
                .favorite(false)
                .deleted(false)
                .build();

        folder = folderRepository.save(folder);
        log.info("Folder created: {} by user {}", folder.getName(), username);

        return enrichFolderResponse(folder);
    }

    @Override
    public FolderResponse getFolder(String username, UUID folderId) {
        UserEntity user = getUser(username);
        FolderEntity folder = folderRepository.findByIdAndOwner(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
        return enrichFolderResponse(folder);
    }

    @Override
    public List<FolderResponse> getFolders(String username, UUID parentFolderId) {
        UserEntity user = getUser(username);
        List<FolderEntity> folders;

        if (parentFolderId == null) {
            folders = folderRepository.findByOwnerAndParentFolderIsNullAndDeletedFalse(user);
        } else {
            FolderEntity parent = folderRepository.findByIdAndOwner(parentFolderId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent folder not found"));
            folders = folderRepository.findByOwnerAndParentFolderAndDeletedFalse(user, parent);
        }

        return folders.stream()
                .map(this::enrichFolderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FolderResponse updateFolder(String username, UUID folderId, UpdateFolderRequest request) {
        UserEntity user = getUser(username);
        FolderEntity folder = folderRepository.findByIdAndOwner(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));

        folder.setName(request.getName());
        if (request.getColor() != null) {
            folder.setColor(request.getColor());
        }

        folder = folderRepository.save(folder);
        log.info("Folder updated: {} by user {}", folderId, username);
        return enrichFolderResponse(folder);
    }

    @Override
    @Transactional
    public FolderResponse toggleFavorite(String username, UUID folderId) {
        UserEntity user = getUser(username);
        FolderEntity folder = folderRepository.findByIdAndOwner(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));

        folder.setFavorite(!folder.isFavorite());
        folder = folderRepository.save(folder);
        return enrichFolderResponse(folder);
    }

    @Override
    @Transactional
    public String deleteFolder(String username, UUID folderId) {
        UserEntity user = getUser(username);
        FolderEntity folder = folderRepository.findByIdAndOwner(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));

        softDeleteSubtree(folder);
        log.info("Folder soft deleted: {} by user {}", folderId, username);
        return "Folder moved to trash";
    }

    private void softDeleteSubtree(FolderEntity folder) {
        folder.setDeleted(true);
        folder.setDeletedAt(LocalDateTime.now());
        folderRepository.save(folder);

        List<FolderEntity> subFolders = folderRepository.findByOwnerAndParentFolderAndDeletedFalse(folder.getOwner(), folder);
        for (FolderEntity sub : subFolders) {
            softDeleteSubtree(sub);
        }
    }

    @Override
    @Transactional
    public String moveFolder(String username, UUID folderId, UUID targetFolderId) {
        UserEntity user = getUser(username);
        FolderEntity folder = folderRepository.findByIdAndOwner(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));

        FolderEntity targetFolder = null;
        if (targetFolderId != null) {
            if (folderId.equals(targetFolderId)) {
                throw new IllegalArgumentException("Cannot move folder into itself");
            }
            targetFolder = folderRepository.findByIdAndOwner(targetFolderId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Target folder not found"));
        }

        folder.setParentFolder(targetFolder);
        folderRepository.save(folder);
        return "Folder moved successfully";
    }

    @Override
    public List<FolderResponse> getFavoriteFolders(String username) {
        UserEntity user = getUser(username);
        return folderRepository.findByOwnerAndFavoriteTrueAndDeletedFalse(user).stream()
                .map(this::enrichFolderResponse)
                .collect(Collectors.toList());
    }

    private FolderResponse enrichFolderResponse(FolderEntity folder) {
        FolderResponse response = folderMapper.toResponse(folder);
        response.setBreadcrumbs(resolveBreadcrumbs(folder));
        response.setChildFoldersCount(folderRepository.findByOwnerAndParentFolderAndDeletedFalse(folder.getOwner(), folder).size());
        response.setChildFilesCount(fileRepository.findByOwnerAndFolderAndDeletedFalse(folder.getOwner(), folder).size());
        return response;
    }

    private List<BreadcrumbResponse> resolveBreadcrumbs(FolderEntity folder) {
        List<BreadcrumbResponse> breadcrumbs = new ArrayList<>();
        FolderEntity curr = folder;
        while (curr != null) {
            breadcrumbs.add(BreadcrumbResponse.builder()
                    .id(curr.getId())
                    .name(curr.getName())
                    .build());
            curr = curr.getParentFolder();
        }
        Collections.reverse(breadcrumbs);
        return breadcrumbs;
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
