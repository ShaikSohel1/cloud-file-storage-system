package com.sohel.cloudstorage.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.response.ActivityResponse;
import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.service.ActivityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getUserActivities(
            Principal principal,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        List<ActivityResponse> activities = activityService.getUserActivities(principal.getName(), limit);
        return ResponseEntity.ok(ApiResponse.success("User activities retrieved", activities));
    }
}
