package com.sohel.cloudstorage.service;

import java.util.List;
import java.util.UUID;

import com.sohel.cloudstorage.dto.request.TagRequest;
import com.sohel.cloudstorage.dto.response.TagResponse;

public interface TagService {
    TagResponse createTag(String username, TagRequest request);
    List<TagResponse> getUserTags(String username);
    String deleteTag(String username, UUID tagId);
    String addTagToFile(String username, UUID fileId, UUID tagId);
    String removeTagFromFile(String username, UUID fileId, UUID tagId);
}
