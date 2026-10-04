package com.sohel.cloudstorage.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.service.PermissionService;
import com.sohel.cloudstorage.service.PreviewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreviewServiceImpl implements PreviewService {

    private final FileRepository fileRepository;
    private final PermissionService permissionService;

    @Override
    public Resource getFilePreview(String username, UUID fileId) {
        if (!permissionService.canReadUserFile(username, fileId)) {
            throw new IllegalArgumentException("Unauthorized to preview this file");
        }

        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        try {
            Path filePath = Paths.get(file.getPath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File resource could not be loaded");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load preview", e);
        }
    }
}
