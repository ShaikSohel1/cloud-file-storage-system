package com.sohel.cloudstorage.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {
    private UUID id;
    private String name;
    private String description;
    private UserProfileResponse owner;
    private List<WorkspaceMemberResponse> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
