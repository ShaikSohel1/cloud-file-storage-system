package com.sohel.cloudstorage.dto.request;

import java.util.UUID;
import lombok.Data;

@Data
public class MoveItemRequest {
    private UUID targetFolderId; // Null means move to root
}
