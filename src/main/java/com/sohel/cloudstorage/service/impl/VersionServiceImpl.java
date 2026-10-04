package com.sohel.cloudstorage.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.response.FileVersionResponse;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FileVersionEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.Phase5Mapper;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.repository.FileVersionRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.PermissionService;
import com.sohel.cloudstorage.service.VersionService;
import com.sohel.cloudstorage.storage.StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    private final FileVersionRepository fileVersionRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final Phase5Mapper phase5Mapper;
    private final StorageService storageService;

    @Override
    public List<FileVersionResponse> getFileVersions(String username, UUID fileId) {
        if (!permissionService.canReadUserFile(username, fileId)) {
            throw new IllegalArgumentException("Unauthorized to view file versions");
        }

        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        return fileVersionRepository.findByFileOrderByVersionNumberDesc(file).stream()
                .map(phase5Mapper::toFileVersionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String restoreVersion(String username, UUID fileId, Integer versionNumber) {
        if (!permissionService.canWriteUserFile(username, fileId)) {
            throw new IllegalArgumentException("Unauthorized to restore file version");
        }

        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        FileVersionEntity targetVersion = fileVersionRepository.findByFileAndVersionNumber(file, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found"));

        file.setVersion(file.getVersion() + 1);
        file.setPath(targetVersion.getPath());
        file.setSize(targetVersion.getSize());
        file.setChecksum(targetVersion.getChecksum());
        
        fileRepository.save(file);

        UserEntity user = userRepository.findByUsername(username).orElseThrow();

        FileVersionEntity newVersion = FileVersionEntity.builder()
                .file(file)
                .versionNumber(file.getVersion())
                .size(targetVersion.getSize())
                .type(targetVersion.getType())
                .path(targetVersion.getPath())
                .checksum(targetVersion.getChecksum())
                .changeDescription("Restored from version " + versionNumber)
                .uploadedBy(user)
                .build();
        fileVersionRepository.save(newVersion);

        log.info("Restored file {} to version {}", fileId, versionNumber);
        return "Version restored successfully";
    }

    @Override
    @Transactional
    public String deleteVersion(String username, UUID fileId, UUID versionId) {
        if (!permissionService.canWriteUserFile(username, fileId)) {
            throw new IllegalArgumentException("Unauthorized to delete file version");
        }

        FileVersionEntity version = fileVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found"));

        if (!version.getFile().getId().equals(fileId)) {
            throw new IllegalArgumentException("Version does not belong to file");
        }

        if (version.getVersionNumber().equals(version.getFile().getVersion())) {
            throw new IllegalArgumentException("Cannot delete the current active version");
        }

        storageService.deleteFile(version.getPath());
        fileVersionRepository.delete(version);

        log.info("Deleted version {} of file {}", version.getVersionNumber(), fileId);
        return "Version deleted successfully";
    }
}
