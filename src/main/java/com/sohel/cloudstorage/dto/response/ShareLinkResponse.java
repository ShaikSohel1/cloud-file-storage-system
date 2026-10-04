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
public class ShareLinkResponse {
    private UUID id;
    private FileResponse file;
    private FolderResponse folder;
    private String token;
    private String publicUrl;
    private boolean passwordProtected;
    private boolean allowDownload;
    private LocalDateTime expiryDate;
    private long viewCount;
    private boolean active;
    private LocalDateTime createdAt;
}
