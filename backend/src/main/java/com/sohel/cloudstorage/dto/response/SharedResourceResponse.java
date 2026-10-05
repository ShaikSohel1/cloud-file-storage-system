package com.sohel.cloudstorage.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sohel.cloudstorage.enums.PermissionRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedResourceResponse {
    private UUID id;
    private FileResponse file;
    private FolderResponse folder;
    private UserProfileResponse sharedWith;
    private UserProfileResponse sharedBy;
    private PermissionRole role;
    private LocalDateTime createdAt;
}
