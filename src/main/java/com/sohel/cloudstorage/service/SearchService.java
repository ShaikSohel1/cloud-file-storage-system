package com.sohel.cloudstorage.service;

import java.util.List;

import com.sohel.cloudstorage.dto.response.FileResponse;
import com.sohel.cloudstorage.dto.response.FolderResponse;

public interface SearchService {
    List<FileResponse> searchFiles(String username, String query);
    List<FolderResponse> searchFolders(String username, String query);
}
