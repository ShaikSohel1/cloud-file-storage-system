package com.sohel.cloudstorage.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.request.CreateShareLinkRequest;
import com.sohel.cloudstorage.dto.request.ShareResourceRequest;
import com.sohel.cloudstorage.dto.response.FileResponse;
import com.sohel.cloudstorage.dto.response.ShareLinkResponse;
import com.sohel.cloudstorage.dto.response.SharedResourceResponse;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FolderEntity;
import com.sohel.cloudstorage.entity.ShareLinkEntity;
import com.sohel.cloudstorage.entity.SharedResourceEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.FileMapper;
import com.sohel.cloudstorage.mapper.ShareMapper;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.repository.FolderRepository;
import com.sohel.cloudstorage.repository.ShareLinkRepository;
import com.sohel.cloudstorage.repository.SharedResourceRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.ShareService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private final SharedResourceRepository sharedResourceRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShareMapper shareMapper;
    private final FileMapper fileMapper;

    @Override
    @Transactional
    public SharedResourceResponse shareResource(String username, ShareResourceRequest request) {
        UserEntity sender = getUser(username);
        UserEntity targetUser = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getUserEmail()));

        FileEntity file = null;
        FolderEntity folder = null;

        if (request.getFileId() != null) {
            file = fileRepository.findByIdAndOwner(request.getFileId(), sender)
                    .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        } else if (request.getFolderId() != null) {
            folder = folderRepository.findByIdAndOwner(request.getFolderId(), sender)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
        } else {
            throw new IllegalArgumentException("Either fileId or folderId must be provided");
        }

        SharedResourceEntity resource = SharedResourceEntity.builder()
                .file(file)
                .folder(folder)
                .sharedWith(targetUser)
                .sharedBy(sender)
                .role(request.getRole())
                .build();

        resource = sharedResourceRepository.save(resource);
        log.info("Shared resource with {}", request.getUserEmail());

        return shareMapper.toResourceResponse(resource);
    }

    @Override
    public List<SharedResourceResponse> getSharedWithMe(String username) {
        UserEntity user = getUser(username);
        return sharedResourceRepository.findBySharedWith(user).stream()
                .map(shareMapper::toResourceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SharedResourceResponse> getSharedByMe(String username) {
        UserEntity user = getUser(username);
        return sharedResourceRepository.findBySharedBy(user).stream()
                .map(shareMapper::toResourceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShareLinkResponse createShareLink(String username, CreateShareLinkRequest request) {
        UserEntity user = getUser(username);

        FileEntity file = null;
        FolderEntity folder = null;

        if (request.getFileId() != null) {
            file = fileRepository.findByIdAndOwner(request.getFileId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        } else if (request.getFolderId() != null) {
            folder = folderRepository.findByIdAndOwner(request.getFolderId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
        } else {
            throw new IllegalArgumentException("Either fileId or folderId must be provided");
        }

        String token = UUID.randomUUID().toString().substring(0, 12);
        String passHash = (request.getPassword() != null && !request.getPassword().isBlank()) ?
                passwordEncoder.encode(request.getPassword()) : null;

        LocalDateTime expiry = request.getExpirationDays() != null ?
                LocalDateTime.now().plusDays(request.getExpirationDays()) : null;

        ShareLinkEntity link = ShareLinkEntity.builder()
                .file(file)
                .folder(folder)
                .token(token)
                .passwordHash(passHash)
                .allowDownload(request.isAllowDownload())
                .expiryDate(expiry)
                .createdBy(user)
                .build();

        link = shareLinkRepository.save(link);

        ShareLinkResponse response = shareMapper.toLinkResponse(link);
        response.setPublicUrl(frontendUrl + "/share/" + token);
        return response;
    }

    @Override
    @Transactional
    public ShareLinkResponse getShareLinkByToken(String token, String password) {
        ShareLinkEntity link = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired share link"));

        if (!link.isActive() || (link.getExpiryDate() != null && link.getExpiryDate().isBefore(LocalDateTime.now()))) {
            throw new IllegalArgumentException("Share link has expired or been disabled");
        }

        if (link.getPasswordHash() != null) {
            if (password == null || !passwordEncoder.matches(password, link.getPasswordHash())) {
                throw new IllegalArgumentException("Password required or incorrect password");
            }
        }

        link.setViewCount(link.getViewCount() + 1);
        shareLinkRepository.save(link);

        ShareLinkResponse response = shareMapper.toLinkResponse(link);
        response.setPublicUrl(frontendUrl + "/share/" + token);
        return response;
    }

    @Override
    public FileResponse accessPublicFile(String token, String password) {
        ShareLinkResponse linkResponse = getShareLinkByToken(token, password);
        return linkResponse.getFile();
    }

    @Override
    @Transactional
    public String disableShareLink(String username, UUID linkId) {
        UserEntity user = getUser(username);
        ShareLinkEntity link = shareLinkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Share link not found"));

        if (!link.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized to disable this share link");
        }

        link.setActive(false);
        shareLinkRepository.save(link);
        return "Share link disabled successfully";
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
