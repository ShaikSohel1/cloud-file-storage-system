package com.sohel.cloudstorage.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.request.ChangePasswordRequest;
import com.sohel.cloudstorage.dto.request.UpdateProfileRequest;
import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.dto.response.UserProfileResponse;
import com.sohel.cloudstorage.dto.response.UserSessionResponse;
import com.sohel.cloudstorage.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(Principal principal) {
        UserProfileResponse response = userService.getCurrentUserProfile(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            Principal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        String result = userService.changePassword(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", result));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<UserSessionResponse>>> getSessions(Principal principal) {
        List<UserSessionResponse> sessions = userService.getUserSessions(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Active sessions retrieved", sessions));
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<String>> revokeSession(Principal principal, @PathVariable("id") Long sessionId) {
        String result = userService.revokeSession(principal.getName(), sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session revoked", result));
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<ApiResponse<String>> revokeAllSessions(Principal principal) {
        String result = userService.revokeAllSessions(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("All sessions revoked", result));
    }
}
