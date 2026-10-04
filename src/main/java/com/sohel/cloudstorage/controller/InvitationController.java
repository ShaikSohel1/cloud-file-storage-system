package com.sohel.cloudstorage.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.dto.response.InvitationResponse;
import com.sohel.cloudstorage.service.InvitationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getMyPendingInvitations(Principal principal) {
        List<InvitationResponse> response = invitationService.getMyPendingInvitations(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Pending invitations retrieved", response));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<String>> acceptInvitation(Principal principal, @PathVariable("id") UUID invitationId) {
        String message = invitationService.acceptInvitation(principal.getName(), invitationId);
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted", message));
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<ApiResponse<String>> declineInvitation(Principal principal, @PathVariable("id") UUID invitationId) {
        String message = invitationService.declineInvitation(principal.getName(), invitationId);
        return ResponseEntity.ok(ApiResponse.success("Invitation declined", message));
    }
}
