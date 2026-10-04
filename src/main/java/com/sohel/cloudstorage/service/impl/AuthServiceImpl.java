package com.sohel.cloudstorage.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.request.ForgotPasswordRequest;
import com.sohel.cloudstorage.dto.request.LoginRequest;
import com.sohel.cloudstorage.dto.request.RefreshTokenRequest;
import com.sohel.cloudstorage.dto.request.RegisterRequest;
import com.sohel.cloudstorage.dto.request.ResetPasswordRequest;
import com.sohel.cloudstorage.dto.response.JwtResponse;
import com.sohel.cloudstorage.dto.response.UserProfileResponse;
import com.sohel.cloudstorage.entity.EmailVerificationTokenEntity;
import com.sohel.cloudstorage.entity.PasswordResetTokenEntity;
import com.sohel.cloudstorage.entity.RefreshTokenEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.entity.UserSessionEntity;
import com.sohel.cloudstorage.enums.AccountStatus;
import com.sohel.cloudstorage.enums.Role;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.UserMapper;
import com.sohel.cloudstorage.repository.EmailVerificationTokenRepository;
import com.sohel.cloudstorage.repository.PasswordResetTokenRepository;
import com.sohel.cloudstorage.repository.RefreshTokenRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.repository.UserSessionRepository;
import com.sohel.cloudstorage.security.CustomUserDetails;
import com.sohel.cloudstorage.security.JwtUtils;
import com.sohel.cloudstorage.service.AuthService;
import com.sohel.cloudstorage.service.EmailService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        UserEntity user = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .emailVerified(true)
                .status(AccountStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        // Create email verification token
        String verificationToken = UUID.randomUUID().toString();
        EmailVerificationTokenEntity tokenEntity = EmailVerificationTokenEntity.builder()
                .user(user)
                .token(verificationToken)
                .expiryDate(Instant.now().plusSeconds(86400)) // 24 hrs
                .build();
        emailVerificationTokenRepository.save(tokenEntity);

        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public JwtResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserEntity user = userDetails.getUser();

        if (!user.isEmailVerified()) {
            // Auto verify in dev if status active, or throw exception
            log.warn("User email not verified: {}", user.getEmail());
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtUtils.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshTokenStr = jwtUtils.generateRefreshToken(user.getUsername());

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiryDate(Instant.now().plusMillis(jwtUtils.getRefreshExpirationMs()))
                .deviceInfo(httpRequest != null ? httpRequest.getHeader("User-Agent") : "Unknown Device")
                .ipAddress(httpRequest != null ? httpRequest.getRemoteAddr() : "127.0.0.1")
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        UserSessionEntity session = UserSessionEntity.builder()
                .user(user)
                .refreshToken(refreshTokenStr)
                .deviceInfo(refreshToken.getDeviceInfo())
                .ipAddress(refreshToken.getIpAddress())
                .lastActive(LocalDateTime.now())
                .build();
        userSessionRepository.save(session);

        log.info("User logged in successfully: {}", user.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .expiresIn(jwtUtils.getJwtExpirationMs() / 1000)
                .user(userMapper.toProfileResponse(user))
                .build();
    }

    @Override
    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token is expired or revoked. Please login again.");
        }

        UserEntity user = refreshToken.getUser();
        String newAccessToken = jwtUtils.generateAccessToken(user.getUsername(), user.getRole().name());

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(requestRefreshToken)
                .expiresIn(jwtUtils.getJwtExpirationMs() / 1000)
                .user(userMapper.toProfileResponse(user))
                .build();
    }

    @Override
    @Transactional
    public String verifyEmail(String token) {
        EmailVerificationTokenEntity tokenEntity = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));

        if (tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            emailVerificationTokenRepository.delete(tokenEntity);
            throw new IllegalArgumentException("Verification token has expired");
        }

        UserEntity user = tokenEntity.getUser();
        user.setEmailVerified(true);
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        emailVerificationTokenRepository.delete(tokenEntity);
        log.info("Email verified for user: {}", user.getEmail());

        return "Email verified successfully. You can now log in.";
    }

    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        passwordResetTokenRepository.deleteByUser(user);

        String resetToken = UUID.randomUUID().toString();
        PasswordResetTokenEntity resetTokenEntity = PasswordResetTokenEntity.builder()
                .user(user)
                .token(resetToken)
                .expiryDate(Instant.now().plusSeconds(3600)) // 1 hr
                .build();
        passwordResetTokenRepository.save(resetTokenEntity);

        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        return "Password reset link has been sent to your email";
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        PasswordResetTokenEntity tokenEntity = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid password reset token"));

        if (tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(tokenEntity);
            throw new IllegalArgumentException("Password reset token has expired");
        }

        UserEntity user = tokenEntity.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(tokenEntity);
        refreshTokenRepository.deleteByUser(user); // Revoke active refresh tokens
        userSessionRepository.deleteByUser(user);

        log.info("Password reset successfully for user: {}", user.getEmail());

        return "Password reset successfully. Please log in with your new password.";
    }

    @Override
    @Transactional
    public String logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.deleteByToken(refreshToken);
            userSessionRepository.deleteByRefreshToken(refreshToken);
        }
        return "Logged out successfully";
    }
}
