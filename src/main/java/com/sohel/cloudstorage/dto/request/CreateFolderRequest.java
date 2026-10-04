package com.sohel.cloudstorage.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateFolderRequest {

    @NotBlank(message = "Folder name is required")
    private String name;

    private UUID parentFolderId;
    private String color;
}
