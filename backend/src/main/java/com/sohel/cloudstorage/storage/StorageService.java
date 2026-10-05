package com.sohel.cloudstorage.storage;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file, String destinationPath) throws IOException;
    byte[] downloadFile(String path) throws IOException;
    boolean deleteFile(String path);
    boolean renameFile(String oldPath, String newPath);
}
