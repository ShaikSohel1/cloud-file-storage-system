package com.sohel.cloudstorage.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagRequest {

    @NotBlank(message = "Tag name cannot be empty")
    private String name;

    private String color;
}
