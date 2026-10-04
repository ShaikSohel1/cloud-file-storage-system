package com.sohel.cloudstorage.dto.request;

import java.util.UUID;
import com.sohel.cloudstorage.enums.PermissionRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShareResourceRequest {
    private UUID fileId;
    private UUID folderId;

    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    private String userEmail;

    @NotNull(message = "Permission role is required")
    private PermissionRole role;
}
