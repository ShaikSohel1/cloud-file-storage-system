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
public class FileResponse {
    private UUID id;
    private String name;
    private String originalName;
    private String extension;
    private Long size;
    private String type;
    private String publicUrl;
    private String thumbnailUrl;
    private String checksum;
    private Integer version;
    private UUID folderId;
    private String folderName;
    private boolean favorite;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private LocalDateTime lastOpenedAt;
}
