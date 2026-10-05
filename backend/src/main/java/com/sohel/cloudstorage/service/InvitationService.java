package com.sohel.cloudstorage.service;

import java.util.List;
import java.util.UUID;

import com.sohel.cloudstorage.dto.response.InvitationResponse;

public interface InvitationService {
    List<InvitationResponse> getMyPendingInvitations(String username);
    String acceptInvitation(String username, UUID invitationId);
    String declineInvitation(String username, UUID invitationId);
}
