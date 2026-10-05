package com.sohel.cloudstorage.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateFolderRequest {

    @NotBlank(message = "Folder name is required")
    private String name;
    private String color;
}
