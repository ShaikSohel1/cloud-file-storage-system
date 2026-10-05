package com.sohel.cloudstorage.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.entity.SubscriptionEntity;
import com.sohel.cloudstorage.service.BillingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/organizations/{orgId}/subscription")
    public ResponseEntity<ApiResponse<SubscriptionEntity>> getSubscription(@PathVariable("orgId") UUID orgId) {
        SubscriptionEntity sub = billingService.getSubscription(orgId);
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved", sub));
    }

    @PostMapping("/organizations/{orgId}/checkout")
    public ResponseEntity<ApiResponse<String>> createCheckoutSession(
            @PathVariable("orgId") UUID orgId,
            @RequestParam("plan") String plan) {
        String url = billingService.createCheckoutSession(orgId, plan);
        return ResponseEntity.ok(ApiResponse.success("Checkout session created", url));
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        billingService.handleStripeWebhook(payload, signature);
        return ResponseEntity.ok("Received");
    }
}
