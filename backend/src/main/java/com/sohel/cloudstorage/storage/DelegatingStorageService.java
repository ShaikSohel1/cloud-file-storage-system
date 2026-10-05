package com.sohel.cloudstorage.storage;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * Primary Delegating Storage Service.
 * Routes file storage requests dynamically to Azure Blob Storage,
 * Supabase Storage, or Local Disk according to configuration.
 */
@Slf4j
@Service
@Primary
public class DelegatingStorageService implements StorageService {

    @Value("${app.storage.type:${STORAGE_TYPE:supabase}}")
    private String storageType;

    private final StorageService azureStorage;
    private final StorageService supabaseStorage;
    private final StorageService localStorage;

    @org.springframework.beans.factory.annotation.Autowired
    public DelegatingStorageService(
            @Qualifier("azureBlobStorageService") StorageService azureStorage,
            @Qualifier("supabaseStorageService") StorageService supabaseStorage,
            @Qualifier("localStorageService") StorageService localStorage) {
        this.azureStorage = azureStorage;
        this.supabaseStorage = supabaseStorage;
        this.localStorage = localStorage;
    }

    public DelegatingStorageService(
            StorageService azureStorage,
            StorageService supabaseStorage,
            StorageService localStorage,
            String storageType) {
        this.azureStorage = azureStorage;
        this.supabaseStorage = supabaseStorage;
        this.localStorage = localStorage;
        this.storageType = storageType;
    }

    private StorageService getTargetStorage() {
        if ("azure".equalsIgnoreCase(storageType)) {
            return azureStorage;
        } else if ("supabase".equalsIgnoreCase(storageType)) {
            return supabaseStorage;
        } else {
            return localStorage;
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String destinationPath) throws IOException {
        StorageService target = getTargetStorage();
        log.debug("Routing upload to storage provider: {}", target.getClass().getSimpleName());
        return target.uploadFile(file, destinationPath);
    }

    @Override
    public byte[] downloadFile(String path) throws IOException {
        StorageService target = getTargetStorage();
        log.debug("Routing download to storage provider: {}", target.getClass().getSimpleName());
        return target.downloadFile(path);
    }

    @Override
    public boolean deleteFile(String path) {
        StorageService target = getTargetStorage();
        log.debug("Routing delete to storage provider: {}", target.getClass().getSimpleName());
        return target.deleteFile(path);
    }

    @Override
    public boolean renameFile(String oldPath, String newPath) {
        StorageService target = getTargetStorage();
        log.debug("Routing rename to storage provider: {}", target.getClass().getSimpleName());
        return target.renameFile(oldPath, newPath);
    }
}
