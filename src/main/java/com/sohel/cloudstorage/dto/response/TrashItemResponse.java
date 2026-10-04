package com.sohel.cloudstorage.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrashItemResponse {
    private UUID id;
    private FileResponse file;
    private FolderResponse folder;
    private FolderResponse originalFolder;
    private String workspaceName;
    private UserProfileResponse deletedBy;
    private LocalDateTime deletedAt;
    private LocalDateTime expirationDate;
}
