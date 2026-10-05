package com.sohel.cloudstorage.dto.response;

import java.time.LocalDateTime;

import com.sohel.cloudstorage.enums.PermissionRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberResponse {
    private Long id;
    private UserProfileResponse user;
    private PermissionRole role;
    private LocalDateTime joinedAt;
}
