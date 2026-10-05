package com.sohel.cloudstorage.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.response.DashboardStatsResponse;
import com.sohel.cloudstorage.dto.response.UserProfileResponse;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.enums.AccountStatus;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.UserMapper;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.repository.FolderRepository;
import com.sohel.cloudstorage.repository.ShareLinkRepository;
import com.sohel.cloudstorage.repository.SharedResourceRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.repository.WorkspaceRepository;
import com.sohel.cloudstorage.service.AdminService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final WorkspaceRepository workspaceRepository;
    private final SharedResourceRepository sharedResourceRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final UserMapper userMapper;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(AccountStatus.ACTIVE);
        long totalFiles = fileRepository.count();
        long totalFolders = folderRepository.count();
        long totalWorkspaces = workspaceRepository.count();
        long totalSharedFiles = sharedResourceRepository.count();
        long totalPublicLinks = shareLinkRepository.count();
        
        long storageUsed = 0L;
        // In a real app we might aggregate via DB query, simulating here
        
        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalFiles(totalFiles)
                .totalFolders(totalFolders)
                .totalWorkspaces(totalWorkspaces)
                .totalSharedFiles(totalSharedFiles)
                .totalPublicLinks(totalPublicLinks)
                .storageUsed(storageUsed)
                .storageRemaining(10000000000L) // Example 10GB system total
                .build();
    }

    @Override
    public Page<UserProfileResponse> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .map(userMapper::toProfileResponse);
    }

    @Override
    @Transactional
    public String disableUser(String adminUsername, String targetEmail) {
        UserEntity user = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(AccountStatus.LOCKED);
        userRepository.save(user);
        return "User suspended";
    }

    @Override
    @Transactional
    public String enableUser(String adminUsername, String targetEmail) {
        UserEntity user = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        return "User activated";
    }
}
