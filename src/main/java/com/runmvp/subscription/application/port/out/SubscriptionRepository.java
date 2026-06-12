package com.runmvp.subscription.application.port.out;

import com.runmvp.subscription.domain.Subscription;
import java.util.Optional;

public interface SubscriptionRepository {
    Subscription save(Subscription subscription);
    Optional<Subscription> findByUserId(Long userId);
    Optional<Subscription> findByPurchaseToken(String purchaseToken);
}
