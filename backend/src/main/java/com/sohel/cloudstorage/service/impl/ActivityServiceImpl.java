package com.sohel.cloudstorage.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.sohel.cloudstorage.dto.response.ActivityResponse;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.ActivityMapper;
import com.sohel.cloudstorage.repository.ActivityRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.ActivityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActivityMapper activityMapper;

    @Override
    public List<ActivityResponse> getUserActivities(String username, int limit) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return activityRepository.findByActorOrderByTimestampDesc(user, PageRequest.of(0, limit)).stream()
                .map(activityMapper::toActivityResponse)
                .collect(Collectors.toList());
    }
}
