package com.sohel.cloudstorage.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.entity.OrganizationEntity;
import com.sohel.cloudstorage.service.OrganizationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationEntity>> createOrganization(
            Principal principal,
            @RequestParam("name") String name) {
        OrganizationEntity org = organizationService.createOrganization(name, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Organization created", org));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationEntity>>> getUserOrganizations(Principal principal) {
        List<OrganizationEntity> orgs = organizationService.getUserOrganizations(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Organizations retrieved", orgs));
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<ApiResponse<OrganizationEntity>> getOrganization(@PathVariable("orgId") UUID orgId) {
        OrganizationEntity org = organizationService.getOrganization(orgId);
        return ResponseEntity.ok(ApiResponse.success("Organization retrieved", org));
    }
}
