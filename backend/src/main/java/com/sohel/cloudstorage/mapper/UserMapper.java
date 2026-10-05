package com.sohel.cloudstorage.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.sohel.cloudstorage.dto.response.UserProfileResponse;
import com.sohel.cloudstorage.dto.response.UserSessionResponse;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.entity.UserSessionEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserProfileResponse toProfileResponse(UserEntity entity);

    UserSessionResponse toSessionResponse(UserSessionEntity entity);
}
