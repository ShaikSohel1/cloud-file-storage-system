package com.sohel.cloudstorage.service;

import java.util.UUID;

import com.sohel.cloudstorage.enums.PermissionRole;

public interface PermissionService {
    boolean canReadUserFile(String username, UUID fileId);
    boolean canWriteUserFile(String username, UUID fileId);
    boolean canReadUserFolder(String username, UUID folderId);
    boolean canWriteUserFolder(String username, UUID folderId);
    PermissionRole getFilePermission(String username, UUID fileId);
}
