package com.runmvp.shared.outbox;

import com.fasterxml.jackson.databind.JsonNode;

public record OutboxEvent(
    String aggregateType,
    Long aggregateId,
    String eventType,
    JsonNode payload
) {
    public static OutboxEvent of(String aggregateType, Long aggregateId,
                                 String eventType, JsonNode payload) {
        return new OutboxEvent(aggregateType, aggregateId, eventType, payload);
    }
}
