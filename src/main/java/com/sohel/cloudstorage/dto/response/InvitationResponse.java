package com.sohel.cloudstorage.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sohel.cloudstorage.enums.InvitationStatus;
import com.sohel.cloudstorage.enums.PermissionRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    private UUID id;
    private String email;
    private UserProfileResponse invitedBy;
    private String workspaceName;
    private String fileName;
    private String folderName;
    private PermissionRole role;
    private InvitationStatus status;
    private String token;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
}
