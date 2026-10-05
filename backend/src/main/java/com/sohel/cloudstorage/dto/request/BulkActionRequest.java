package com.sohel.cloudstorage.dto.request;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class BulkActionRequest {
    private List<UUID> fileIds;
    private List<UUID> folderIds;
    private UUID targetFolderId;
}
