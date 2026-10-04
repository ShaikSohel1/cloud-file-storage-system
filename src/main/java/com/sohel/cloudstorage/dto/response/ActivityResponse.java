package com.sohel.cloudstorage.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sohel.cloudstorage.enums.ActivityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {
    private UUID id;
    private UserProfileResponse actor;
    private String workspaceName;
    private String fileName;
    private String folderName;
    private ActivityType activityType;
    private String description;
    private LocalDateTime timestamp;
}
