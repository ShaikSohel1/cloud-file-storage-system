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
public class FileVersionResponse {
    private UUID id;
    private UUID fileId;
    private Integer versionNumber;
    private Long size;
    private String type;
    private String checksum;
    private String changeDescription;
    private UserProfileResponse uploadedBy;
    private LocalDateTime uploadDate;
}
