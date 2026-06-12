package com.runmvp.subscription.application.port.out;

import java.time.Instant;

public interface BillingEventRepository {
    record Event(String purchaseToken, String notificationType,
                 String rawPayload, Instant receivedAt) {}
    void save(Event event);
}
