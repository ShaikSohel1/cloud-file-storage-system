package com.sohel.cloudstorage.service;

import java.util.List;

import com.sohel.cloudstorage.dto.request.ChangePasswordRequest;
import com.sohel.cloudstorage.dto.request.UpdateProfileRequest;
import com.sohel.cloudstorage.dto.response.UserProfileResponse;
import com.sohel.cloudstorage.dto.response.UserSessionResponse;

public interface UserService {
    UserProfileResponse getCurrentUserProfile(String username);
    UserProfileResponse updateProfile(String username, UpdateProfileRequest request);
    String changePassword(String username, ChangePasswordRequest request);
    List<UserSessionResponse> getUserSessions(String username);
    String revokeSession(String username, Long sessionId);
    String revokeAllSessions(String username);
}
