package com.sohel.cloudstorage.service;

import java.util.List;
import com.sohel.cloudstorage.dto.response.ActivityResponse;

public interface ActivityService {
    List<ActivityResponse> getUserActivities(String username, int limit);
}
