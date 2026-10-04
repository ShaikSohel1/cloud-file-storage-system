package com.sohel.cloudstorage.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sohel.cloudstorage.dto.response.FileResponse;
import com.sohel.cloudstorage.dto.response.FolderResponse;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.FileMapper;
import com.sohel.cloudstorage.mapper.FolderMapper;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.repository.FolderRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.SearchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final FileMapper fileMapper;
    private final FolderMapper folderMapper;

    @Override
    public List<FileResponse> searchFiles(String username, String query) {
        UserEntity user = getUser(username);
        return fileRepository.findByNameContainingIgnoreCaseAndOwnerAndDeletedFalse(query, user).stream()
                .map(fileMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FolderResponse> searchFolders(String username, String query) {
        UserEntity user = getUser(username);
        return folderRepository.findByNameContainingIgnoreCaseAndOwnerAndDeletedFalse(query, user).stream()
                .map(folderMapper::toResponse)
                .collect(Collectors.toList());
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
