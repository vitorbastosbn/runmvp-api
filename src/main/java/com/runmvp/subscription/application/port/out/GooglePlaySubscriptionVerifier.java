package com.runmvp.subscription.application.port.out;

import java.time.Instant;
import java.util.Set;

public interface GooglePlaySubscriptionVerifier {

    record VerifiedSubscription(
        String subscriptionState,
        String productId,
        Instant expiryTime
    ) {
        public boolean isEntitled() {
            return Set.of(
                "SUBSCRIPTION_STATE_ACTIVE",
                "SUBSCRIPTION_STATE_CANCELED",
                "SUBSCRIPTION_STATE_IN_GRACE_PERIOD"
            ).contains(subscriptionState) && expiryTime.isAfter(Instant.now());
        }
    }

    VerifiedSubscription verify(String purchaseToken, String productId);
}
