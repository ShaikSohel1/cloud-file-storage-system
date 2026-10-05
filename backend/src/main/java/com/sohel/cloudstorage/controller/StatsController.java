package com.sohel.cloudstorage.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.dto.response.StorageStatsResponse;
import com.sohel.cloudstorage.service.StatisticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatisticsService statisticsService;

    @GetMapping("/storage")
    public ResponseEntity<ApiResponse<StorageStatsResponse>> getStorageStats(Principal principal) {
        StorageStatsResponse stats = statisticsService.getStorageStats(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Storage analytics retrieved", stats));
    }
}
