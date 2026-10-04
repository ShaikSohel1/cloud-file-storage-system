package com.sohel.cloudstorage.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FolderEntity;
import com.sohel.cloudstorage.entity.SharedResourceEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.enums.PermissionRole;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.repository.FolderRepository;
import com.sohel.cloudstorage.repository.SharedResourceRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.PermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final SharedResourceRepository sharedResourceRepository;

    @Override
    public boolean canReadUserFile(String username, UUID fileId) {
        PermissionRole role = getFilePermission(username, fileId);
        return role != null;
    }

    @Override
    public boolean canWriteUserFile(String username, UUID fileId) {
        PermissionRole role = getFilePermission(username, fileId);
        return role == PermissionRole.OWNER || role == PermissionRole.ADMIN || role == PermissionRole.EDITOR;
    }

    @Override
    public boolean canReadUserFolder(String username, UUID folderId) {
        UserEntity user = getUser(username);
        Optional<FolderEntity> folder = folderRepository.findById(folderId);
        if (folder.isPresent() && folder.get().getOwner().getId().equals(user.getId())) {
            return true;
        }
        if (folder.isPresent()) {
            Optional<SharedResourceEntity> shared = sharedResourceRepository.findByFolderAndSharedWith(folder.get(), user);
            return shared.isPresent();
        }
        return false;
    }

    @Override
    public boolean canWriteUserFolder(String username, UUID folderId) {
        UserEntity user = getUser(username);
        Optional<FolderEntity> folder = folderRepository.findById(folderId);
        if (folder.isPresent() && folder.get().getOwner().getId().equals(user.getId())) {
            return true;
        }
        if (folder.isPresent()) {
            Optional<SharedResourceEntity> shared = sharedResourceRepository.findByFolderAndSharedWith(folder.get(), user);
            if (shared.isPresent()) {
                PermissionRole role = shared.get().getRole();
                return role == PermissionRole.OWNER || role == PermissionRole.ADMIN || role == PermissionRole.EDITOR;
            }
        }
        return false;
    }

    @Override
    public PermissionRole getFilePermission(String username, UUID fileId) {
        UserEntity user = getUser(username);
        Optional<FileEntity> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty()) return null;

        FileEntity file = fileOpt.get();
        if (file.getOwner() != null && file.getOwner().getId().equals(user.getId())) {
            return PermissionRole.OWNER;
        }

        Optional<SharedResourceEntity> shared = sharedResourceRepository.findByFileAndSharedWith(file, user);
        return shared.map(SharedResourceEntity::getRole).orElse(null);
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}
