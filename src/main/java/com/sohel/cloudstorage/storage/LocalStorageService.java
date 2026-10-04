package com.sohel.cloudstorage.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LocalStorageService implements StorageService {

    @Override
    public String uploadFile(MultipartFile file, String destinationPath) throws IOException {
        File folder = new File(destinationPath).getParentFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File destFile = new File(destinationPath);
        file.transferTo(destFile);
        log.info("File saved locally at: {}", destinationPath);
        return destinationPath;
    }

    @Override
    public byte[] downloadFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            log.warn("File not found locally at: {}", path);
            return null;
        }
        return Files.readAllBytes(file.toPath());
    }

    @Override
    public boolean deleteFile(String path) {
        File file = new File(path);
        boolean deleted = file.exists() && file.delete();
        if (deleted) {
            log.info("File deleted locally: {}", path);
        } else {
            log.warn("Failed to delete local file or file not found: {}", path);
        }
        return deleted;
    }

    @Override
    public boolean renameFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        boolean renamed = oldFile.renameTo(newFile);
        if (renamed) {
            log.info("File renamed locally from {} to {}", oldPath, newPath);
        } else {
            log.warn("Failed to rename local file from {} to {}", oldPath, newPath);
        }
        return renamed;
    }
}
