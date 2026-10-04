package com.sohel.cloudstorage.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.sohel.cloudstorage.dto.response.FileResponse;
import com.sohel.cloudstorage.entity.FileEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileMapper {

    @Mapping(target = "folderId", source = "folder.id")
    @Mapping(target = "folderName", source = "folder.name")
    FileResponse toResponse(FileEntity entity);
}
