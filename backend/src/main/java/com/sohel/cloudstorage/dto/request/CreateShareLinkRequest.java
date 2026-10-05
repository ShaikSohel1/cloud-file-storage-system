package com.sohel.cloudstorage.dto.request;

import java.util.UUID;
import lombok.Data;

@Data
public class CreateShareLinkRequest {
    private UUID fileId;
    private UUID folderId;
    private String password;
    private boolean allowDownload = true;
    private Integer expirationDays;
}
