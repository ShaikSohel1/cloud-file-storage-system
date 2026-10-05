package com.sohel.cloudstorage.service;

import java.util.List;
import java.util.UUID;

import com.sohel.cloudstorage.dto.response.FileResponse;
import com.sohel.cloudstorage.dto.response.FolderResponse;

public interface TrashService {
    List<FileResponse> getTrashedFiles(String username);
    List<FolderResponse> getTrashedFolders(String username);
    String restoreFile(String username, UUID fileId);
    String restoreFolder(String username, UUID folderId);
    String permanentlyDeleteFile(String username, UUID fileId);
    String permanentlyDeleteFolder(String username, UUID folderId);
    String emptyTrash(String username);
}
