package com.sohel.cloudstorage.service;

import java.util.List;
import java.util.UUID;

import com.sohel.cloudstorage.dto.request.CreateShareLinkRequest;
import com.sohel.cloudstorage.dto.request.ShareResourceRequest;
import com.sohel.cloudstorage.dto.response.FileResponse;
import com.sohel.cloudstorage.dto.response.ShareLinkResponse;
import com.sohel.cloudstorage.dto.response.SharedResourceResponse;

public interface ShareService {
    SharedResourceResponse shareResource(String username, ShareResourceRequest request);
    List<SharedResourceResponse> getSharedWithMe(String username);
    List<SharedResourceResponse> getSharedByMe(String username);
    ShareLinkResponse createShareLink(String username, CreateShareLinkRequest request);
    ShareLinkResponse getShareLinkByToken(String token, String password);
    FileResponse accessPublicFile(String token, String password);
    String disableShareLink(String username, UUID linkId);
}
