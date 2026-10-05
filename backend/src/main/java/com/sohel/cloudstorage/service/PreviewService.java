package com.sohel.cloudstorage.service;

import java.util.UUID;
import org.springframework.core.io.Resource;

public interface PreviewService {
    Resource getFilePreview(String username, UUID fileId);
}
