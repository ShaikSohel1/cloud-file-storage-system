package com.sohel.cloudstorage.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;
import com.sohel.cloudstorage.dto.request.BulkActionRequest;
import com.sohel.cloudstorage.dto.response.FileResponse;

public interface FileService {
    FileResponse uploadFile(String username, MultipartFile file, UUID folderId) throws IOException;
    byte[] downloadFile(String username, UUID fileId) throws IOException;
    List<FileResponse> getFiles(String username, UUID folderId);
    FileResponse toggleFavorite(String username, UUID fileId);
    String softDeleteFile(String username, UUID fileId);
    FileResponse renameFile(String username, UUID fileId, String newName);
    FileResponse moveFile(String username, UUID fileId, UUID targetFolderId);
    String handleBulkActions(String username, String action, BulkActionRequest request);
    List<FileResponse> getFavoriteFiles(String username);
}
