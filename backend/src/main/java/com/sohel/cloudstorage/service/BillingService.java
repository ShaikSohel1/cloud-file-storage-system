package com.sohel.cloudstorage.service;

import java.util.UUID;
import com.sohel.cloudstorage.entity.SubscriptionEntity;

public interface BillingService {
    SubscriptionEntity getSubscription(UUID organizationId);
    String createCheckoutSession(UUID organizationId, String planName);
    void handleStripeWebhook(String payload, String signature);
}
