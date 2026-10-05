package com.sohel.cloudstorage.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.request.TagRequest;
import com.sohel.cloudstorage.dto.response.TagResponse;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.FileTagEntity;
import com.sohel.cloudstorage.entity.TagEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.Phase5Mapper;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.repository.FileTagRepository;
import com.sohel.cloudstorage.repository.TagRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.TagService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final FileTagRepository fileTagRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final Phase5Mapper phase5Mapper;

    @Override
    @Transactional
    public TagResponse createTag(String username, TagRequest request) {
        UserEntity owner = getUser(username);
        
        if (tagRepository.findByNameAndOwner(request.getName(), owner).isPresent()) {
            throw new IllegalArgumentException("Tag with this name already exists");
        }

        TagEntity tag = TagEntity.builder()
                .name(request.getName())
                .color(request.getColor())
                .owner(owner)
                .build();
        tag = tagRepository.save(tag);
        return phase5Mapper.toTagResponse(tag);
    }

    @Override
    public List<TagResponse> getUserTags(String username) {
        UserEntity owner = getUser(username);
        return tagRepository.findByOwner(owner).stream()
                .map(phase5Mapper::toTagResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String deleteTag(String username, UUID tagId) {
        UserEntity owner = getUser(username);
        TagEntity tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        if (!tag.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Unauthorized to delete this tag");
        }

        List<FileTagEntity> fileTags = fileTagRepository.findByTag(tag);
        fileTagRepository.deleteAll(fileTags);
        tagRepository.delete(tag);
        return "Tag deleted successfully";
    }

    @Override
    @Transactional
    public String addTagToFile(String username, UUID fileId, UUID tagId) {
        UserEntity owner = getUser(username);
        FileEntity file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("File not found or unauthorized"));
        
        TagEntity tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        if (fileTagRepository.findByFileAndTag(file, tag).isPresent()) {
            return "Tag already added to file";
        }

        FileTagEntity fileTag = FileTagEntity.builder()
                .file(file)
                .tag(tag)
                .build();
        fileTagRepository.save(fileTag);
        return "Tag added to file";
    }

    @Override
    @Transactional
    public String removeTagFromFile(String username, UUID fileId, UUID tagId) {
        UserEntity owner = getUser(username);
        FileEntity file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("File not found or unauthorized"));
        
        TagEntity tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        FileTagEntity fileTag = fileTagRepository.findByFileAndTag(file, tag)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found on this file"));
        
        fileTagRepository.delete(fileTag);
        return "Tag removed from file";
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
