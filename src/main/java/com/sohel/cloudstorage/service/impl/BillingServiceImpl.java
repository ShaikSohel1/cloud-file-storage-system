package com.sohel.cloudstorage.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sohel.cloudstorage.entity.SubscriptionEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.repository.OrganizationRepository;
import com.sohel.cloudstorage.repository.SubscriptionRepository;
import com.sohel.cloudstorage.service.BillingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final SubscriptionRepository subscriptionRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    public SubscriptionEntity getSubscription(UUID organizationId) {
        return subscriptionRepository.findByOrganization(
            organizationRepository.findById(organizationId).orElseThrow(() -> new ResourceNotFoundException("Org not found"))
        ).orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
    }

    @Override
    public String createCheckoutSession(UUID organizationId, String planName) {
        // Simulated Stripe Checkout Session creation
        log.info("Creating simulated Stripe checkout session for org {} on plan {}", organizationId, planName);
        return "https://checkout.stripe.com/pay/cs_test_mock_" + UUID.randomUUID().toString();
    }

    @Override
    public void handleStripeWebhook(String payload, String signature) {
        // Simulated webhook handler
        log.info("Received simulated Stripe webhook. Signature: {}", signature);
        // In reality, we'd verify the signature and update the SubscriptionEntity status
    }
}
