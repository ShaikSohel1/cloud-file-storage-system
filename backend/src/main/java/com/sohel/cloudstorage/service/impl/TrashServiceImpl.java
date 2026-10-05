package com.sohel.cloudstorage.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.response.FileResponse;
import com.sohel.cloudstorage.dto.response.FolderResponse;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FolderEntity;
import com.sohel.cloudstorage.entity.TrashItemEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.FileMapper;
import com.sohel.cloudstorage.mapper.FolderMapper;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.repository.FolderRepository;
import com.sohel.cloudstorage.repository.TrashItemRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.storage.StorageService;
import com.sohel.cloudstorage.service.TrashService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrashServiceImpl implements TrashService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final TrashItemRepository trashItemRepository;
    private final FileMapper fileMapper;
    private final FolderMapper folderMapper;
    private final StorageService storageService;

    @Override
    public List<FileResponse> getTrashedFiles(String username) {
        UserEntity user = getUser(username);
        return fileRepository.findByOwnerAndDeletedTrue(user).stream()
                .map(fileMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FolderResponse> getTrashedFolders(String username) {
        UserEntity user = getUser(username);
        return folderRepository.findByOwnerAndDeletedTrue(user).stream()
                .map(folderMapper::toResponse)
                .collect(Collectors.toList());
    }

    // A helper method for FileService/FolderService to call when moving items to trash
    @Transactional
    public void moveToTrash(FileEntity file, FolderEntity folder, UserEntity deletedBy) {
        TrashItemEntity trashItem = TrashItemEntity.builder()
                .file(file)
                .folder(folder)
                .originalFolder(file != null ? file.getFolder() : folder.getParentFolder())
                .workspace(file != null ? file.getWorkspace() : folder.getWorkspace())
                .deletedBy(deletedBy)
                .build();
        trashItemRepository.save(trashItem);
    }

    @Override
    @Transactional
    public String restoreFile(String username, UUID fileId) {
        UserEntity user = getUser(username);
        FileEntity file = fileRepository.findByIdAndOwner(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        file.setDeleted(false);
        file.setDeletedAt(null);
        fileRepository.save(file);
        
        // Remove from trash items
        trashItemRepository.findAll().stream()
                .filter(t -> t.getFile() != null && t.getFile().getId().equals(fileId))
                .findFirst().ifPresent(trashItemRepository::delete);

        log.info("Restored file {} for user {}", fileId, username);
        return "File restored successfully";
    }

    @Override
    @Transactional
    public String restoreFolder(String username, UUID folderId) {
        UserEntity user = getUser(username);
        FolderEntity folder = folderRepository.findByIdAndOwner(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));

        restoreFolderSubtree(folder);
        
        trashItemRepository.findAll().stream()
                .filter(t -> t.getFolder() != null && t.getFolder().getId().equals(folderId))
                .findFirst().ifPresent(trashItemRepository::delete);

        log.info("Restored folder {} for user {}", folderId, username);
        return "Folder restored successfully";
    }

    private void restoreFolderSubtree(FolderEntity folder) {
        folder.setDeleted(false);
        folder.setDeletedAt(null);
        folderRepository.save(folder);

        List<FolderEntity> subFolders = folderRepository.findByOwnerAndDeletedTrue(folder.getOwner());
        for (FolderEntity sub : subFolders) {
            if (sub.getParentFolder() != null && sub.getParentFolder().getId().equals(folder.getId())) {
                restoreFolderSubtree(sub);
            }
        }
    }

    @Override
    @Transactional
    public String permanentlyDeleteFile(String username, UUID fileId) {
        UserEntity user = getUser(username);
        FileEntity file = fileRepository.findByIdAndOwner(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        trashItemRepository.findAll().stream()
                .filter(t -> t.getFile() != null && t.getFile().getId().equals(fileId))
                .findFirst().ifPresent(trashItemRepository::delete);

        storageService.deleteFile(file.getPath());
        fileRepository.delete(file);
        log.info("Permanently deleted file {} for user {}", fileId, username);
        return "File permanently deleted";
    }

    @Override
    @Transactional
    public String permanentlyDeleteFolder(String username, UUID folderId) {
        UserEntity user = getUser(username);
        FolderEntity folder = folderRepository.findByIdAndOwner(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));

        trashItemRepository.findAll().stream()
                .filter(t -> t.getFolder() != null && t.getFolder().getId().equals(folderId))
                .findFirst().ifPresent(trashItemRepository::delete);

        deleteFolderSubtree(folder);
        log.info("Permanently deleted folder {} for user {}", folderId, username);
        return "Folder permanently deleted";
    }

    private void deleteFolderSubtree(FolderEntity folder) {
        List<FileEntity> files = fileRepository.findByOwnerAndFolderAndDeletedFalse(folder.getOwner(), folder);
        for (FileEntity file : files) {
            storageService.deleteFile(file.getPath());
            fileRepository.delete(file);
        }

        List<FolderEntity> subFolders = folderRepository.findByOwnerAndDeletedTrue(folder.getOwner());
        for (FolderEntity sub : subFolders) {
            if (sub.getParentFolder() != null && sub.getParentFolder().getId().equals(folder.getId())) {
                deleteFolderSubtree(sub);
            }
        }

        folderRepository.delete(folder);
    }

    @Override
    @Transactional
    public String emptyTrash(String username) {
        UserEntity user = getUser(username);

        List<FileEntity> trashedFiles = fileRepository.findByOwnerAndDeletedTrue(user);
        for (FileEntity file : trashedFiles) {
            storageService.deleteFile(file.getPath());
            fileRepository.delete(file);
        }

        List<FolderEntity> trashedFolders = folderRepository.findByOwnerAndDeletedTrue(user);
        for (FolderEntity folder : trashedFolders) {
            folderRepository.delete(folder);
        }
        
        List<TrashItemEntity> trashItems = trashItemRepository.findByDeletedByOrderByDeletedAtDesc(user);
        trashItemRepository.deleteAll(trashItems);

        log.info("Emptied trash for user {}", username);
        return "Trash emptied successfully";
    }
    
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional
    public void cleanupExpiredTrash() {
        log.info("Running expired trash cleanup job");
        List<TrashItemEntity> expiredItems = trashItemRepository.findByExpirationDateBefore(LocalDateTime.now());
        
        for (TrashItemEntity item : expiredItems) {
            if (item.getFile() != null) {
                storageService.deleteFile(item.getFile().getPath());
                fileRepository.delete(item.getFile());
            } else if (item.getFolder() != null) {
                deleteFolderSubtree(item.getFolder());
            }
            trashItemRepository.delete(item);
        }
        log.info("Cleaned up {} expired trash items", expiredItems.size());
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
