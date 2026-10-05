package com.sohel.cloudstorage.service;

import java.util.List;
import java.util.UUID;

import com.sohel.cloudstorage.dto.response.FileVersionResponse;

public interface VersionService {
    List<FileVersionResponse> getFileVersions(String username, UUID fileId);
    String restoreVersion(String username, UUID fileId, Integer versionNumber);
    String deleteVersion(String username, UUID fileId, UUID versionId);
}
