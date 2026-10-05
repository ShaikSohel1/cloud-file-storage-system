package com.sohel.cloudstorage.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sohel.cloudstorage.enums.AccountStatus;
import com.sohel.cloudstorage.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String profilePhoto;
    private Role role;
    private boolean emailVerified;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Long storageUsed;
    private Long storageLimit;
}
