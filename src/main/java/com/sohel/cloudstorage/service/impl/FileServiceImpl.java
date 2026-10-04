package com.sohel.cloudstorage.service.impl;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sohel.cloudstorage.constants.ApplicationConstants;
import com.sohel.cloudstorage.dto.request.BulkActionRequest;
import com.sohel.cloudstorage.dto.response.FileResponse;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FolderEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.FileMapper;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.repository.FolderRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.FileService;
import com.sohel.cloudstorage.storage.StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${app.storage.local.folder-path}")
    private String folderPath;

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final FileMapper fileMapper;
    private final StorageService storageService;

    @Override
    @Transactional
    public FileResponse uploadFile(String username, MultipartFile file, UUID folderId) throws IOException {
        UserEntity user = getUser(username);

        FolderEntity folder = null;
        if (folderId != null) {
            folder = folderRepository.findByIdAndOwner(folderId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) originalName = "unnamed_file";

        String extension = "";
        int dotIdx = originalName.lastIndexOf(".");
        if (dotIdx >= 0) {
            extension = originalName.substring(dotIdx + 1);
        }

        String uniqueFileName = UUID.randomUUID() + "_" + originalName.replaceAll(" ", "_");
        String filePath = folderPath + (user.getId() != null ? user.getId() + "/" : "") + uniqueFileName;

        byte[] fileBytes = file.getBytes();
        String checksum = computeChecksum(fileBytes);

        storageService.uploadFile(file, filePath);

        String relativeKey = (user.getId() != null ? user.getId() + "/" : "") + uniqueFileName;
        String publicUrl = "https://sqatowxytdyauwqlmkcx.supabase.co/storage/v1/object/public/cloudstorage/" + relativeKey;

        FileEntity fileEntity = FileEntity.builder()
                .name(originalName)
                .originalName(originalName)
                .extension(extension)
                .size(file.getSize())
                .type(file.getContentType())
                .path(filePath)
                .publicUrl(publicUrl)
                .checksum(checksum)
                .version(1)
                .owner(user)
                .folder(folder)
                .favorite(false)
                .deleted(false)
                .build();

        fileEntity = fileRepository.save(fileEntity);

        // Update user storage used
        Long currentUsed = user.getStorageUsed() != null ? user.getStorageUsed() : 0L;
        user.setStorageUsed(currentUsed + file.getSize());
        userRepository.save(user);

        log.info("File uploaded: {} by user {}", originalName, username);
        return fileMapper.toResponse(fileEntity);
    }

    @Override
    @Transactional
    public byte[] downloadFile(String username, UUID fileId) throws IOException {
        UserEntity user = getUser(username);
        FileEntity fileEntity = fileRepository.findByIdAndOwner(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ApplicationConstants.FILE_NOT_FOUND));

        fileEntity.setLastOpenedAt(LocalDateTime.now());
        fileRepository.save(fileEntity);

        byte[] data = storageService.downloadFile(fileEntity.getPath());
        if (data == null) {
            throw new ResourceNotFoundException(ApplicationConstants.FILE_NOT_FOUND);
        }
        return data;
    }

    @Override
    public List<FileResponse> getFiles(String username, UUID folderId) {
        UserEntity user = getUser(username);
        List<FileEntity> files;

        if (folderId == null) {
            files = fileRepository.findByOwnerAndFolderIsNullAndDeletedFalse(user);
        } else {
            FolderEntity folder = folderRepository.findByIdAndOwner(folderId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
            files = fileRepository.findByOwnerAndFolderAndDeletedFalse(user, folder);
        }

        return files.stream()
                .map(fileMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FileResponse toggleFavorite(String username, UUID fileId) {
        UserEntity user = getUser(username);
        FileEntity file = fileRepository.findByIdAndOwner(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        file.setFavorite(!file.isFavorite());
        file = fileRepository.save(file);
        return fileMapper.toResponse(file);
    }

    @Override
    @Transactional
    public String softDeleteFile(String username, UUID fileId) {
        UserEntity user = getUser(username);
        FileEntity file = fileRepository.findByIdAndOwner(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        file.setDeleted(true);
        file.setDeletedAt(LocalDateTime.now());
        fileRepository.save(file);

        log.info("File soft deleted: {} by user {}", fileId, username);
        return ApplicationConstants.FILE_DELETED_SUCCESS;
    }

    @Override
    @Transactional
    public FileResponse renameFile(String username, UUID fileId, String newName) {
        UserEntity user = getUser(username);
        FileEntity file = fileRepository.findByIdAndOwner(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        file.setName(newName);
        file = fileRepository.save(file);
        return fileMapper.toResponse(file);
    }

    @Override
    @Transactional
    public FileResponse moveFile(String username, UUID fileId, UUID targetFolderId) {
        UserEntity user = getUser(username);
        FileEntity file = fileRepository.findByIdAndOwner(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        FolderEntity targetFolder = null;
        if (targetFolderId != null) {
            targetFolder = folderRepository.findByIdAndOwner(targetFolderId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Target folder not found"));
        }

        file.setFolder(targetFolder);
        file = fileRepository.save(file);
        return fileMapper.toResponse(file);
    }

    @Override
    @Transactional
    public String handleBulkActions(String username, String action, BulkActionRequest request) {
        UserEntity user = getUser(username);

        if (request.getFileIds() != null) {
            for (UUID fileId : request.getFileIds()) {
                FileEntity file = fileRepository.findByIdAndOwner(fileId, user).orElse(null);
                if (file != null) {
                    if ("delete".equalsIgnoreCase(action)) {
                        file.setDeleted(true);
                        file.setDeletedAt(LocalDateTime.now());
                    } else if ("favorite".equalsIgnoreCase(action)) {
                        file.setFavorite(true);
                    } else if ("move".equalsIgnoreCase(action) && request.getTargetFolderId() != null) {
                        FolderEntity target = folderRepository.findByIdAndOwner(request.getTargetFolderId(), user).orElse(null);
                        file.setFolder(target);
                    }
                    fileRepository.save(file);
                }
            }
        }

        return "Bulk operation completed successfully";
    }

    @Override
    public List<FileResponse> getFavoriteFiles(String username) {
        UserEntity user = getUser(username);
        return fileRepository.findByOwnerAndFavoriteTrueAndDeletedFalse(user).stream()
                .map(fileMapper::toResponse)
                .collect(Collectors.toList());
    }

    private String computeChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .or(() -> {
                    try {
                        return userRepository.findById(UUID.fromString(username));
                    } catch (Exception e) {
                        return java.util.Optional.empty();
                    }
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
