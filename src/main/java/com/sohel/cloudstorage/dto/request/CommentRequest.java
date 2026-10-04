package com.sohel.cloudstorage.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {

    @NotNull(message = "File ID is required")
    private UUID fileId;

    @NotBlank(message = "Comment content cannot be empty")
    private String content;

    private UUID parentCommentId;
}
