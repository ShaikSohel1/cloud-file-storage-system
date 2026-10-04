package com.sohel.cloudstorage.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class BulkOperationRequest {
    
    @NotEmpty(message = "File IDs list cannot be empty if no folders are provided")
    private List<UUID> fileIds;
    
    private List<UUID> folderIds;

    // Optional fields depending on the operation
    private UUID targetFolderId;
    private UUID targetWorkspaceId;
}
