package com.sohel.cloudstorage.service;

import com.sohel.cloudstorage.dto.request.ForgotPasswordRequest;
import com.sohel.cloudstorage.dto.request.LoginRequest;
import com.sohel.cloudstorage.dto.request.RefreshTokenRequest;
import com.sohel.cloudstorage.dto.request.RegisterRequest;
import com.sohel.cloudstorage.dto.request.ResetPasswordRequest;
import com.sohel.cloudstorage.dto.response.JwtResponse;
import com.sohel.cloudstorage.dto.response.UserProfileResponse;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    UserProfileResponse register(RegisterRequest request);
    JwtResponse login(LoginRequest request, HttpServletRequest httpRequest);
    JwtResponse refreshToken(RefreshTokenRequest request);
    String verifyEmail(String token);
    String forgotPassword(ForgotPasswordRequest request);
    String resetPassword(ResetPasswordRequest request);
    String logout(String refreshToken);
}
