package com.sohel.cloudstorage.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.request.ChangePasswordRequest;
import com.sohel.cloudstorage.dto.request.UpdateProfileRequest;
import com.sohel.cloudstorage.dto.response.UserProfileResponse;
import com.sohel.cloudstorage.dto.response.UserSessionResponse;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.entity.UserSessionEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.UserMapper;
import com.sohel.cloudstorage.repository.RefreshTokenRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.repository.UserSessionRepository;
import com.sohel.cloudstorage.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserProfileResponse getCurrentUserProfile(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        if (request.getProfilePhoto() != null) {
            user.setProfilePhoto(request.getProfilePhoto());
        }

        user = userRepository.save(user);
        log.info("Updated profile for user: {}", username);

        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public String changePassword(String username, ChangePasswordRequest request) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", username);
        return "Password updated successfully";
    }

    @Override
    public List<UserSessionResponse> getUserSessions(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userSessionRepository.findByUser(user).stream()
                .map(userMapper::toSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String revokeSession(String username, Long sessionId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserSessionEntity session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized to revoke this session");
        }

        refreshTokenRepository.deleteByToken(session.getRefreshToken());
        userSessionRepository.delete(session);

        log.info("Session {} revoked for user {}", sessionId, username);
        return "Session revoked successfully";
    }

    @Override
    @Transactional
    public String revokeAllSessions(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenRepository.deleteByUser(user);
        userSessionRepository.deleteByUser(user);

        log.info("All sessions revoked for user {}", username);
        return "All sessions revoked successfully";
    }
}
