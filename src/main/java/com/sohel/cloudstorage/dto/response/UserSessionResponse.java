package com.sohel.cloudstorage.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionResponse {

    private Long id;
    private String deviceInfo;
    private String ipAddress;
    private LocalDateTime lastActive;
    private boolean currentSession;
}
