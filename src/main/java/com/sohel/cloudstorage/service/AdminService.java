package com.sohel.cloudstorage.service;

import org.springframework.data.domain.Page;
import com.sohel.cloudstorage.dto.response.DashboardStatsResponse;
import com.sohel.cloudstorage.dto.response.UserProfileResponse;

public interface AdminService {
    DashboardStatsResponse getDashboardStats();
    Page<UserProfileResponse> getAllUsers(int page, int size);
    String disableUser(String adminUsername, String targetEmail);
    String enableUser(String adminUsername, String targetEmail);
}
