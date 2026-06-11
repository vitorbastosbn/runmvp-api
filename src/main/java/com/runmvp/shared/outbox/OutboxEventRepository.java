package com.runmvp.shared.outbox;

public interface OutboxEventRepository {
    void publish(OutboxEvent event);
}
