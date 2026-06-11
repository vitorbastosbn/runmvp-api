package com.runmvp.shared.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity
@Table(name = "outbox_events")
class OutboxEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 40)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    static OutboxEventJpaEntity fromDomain(OutboxEvent event) {
        OutboxEventJpaEntity e = new OutboxEventJpaEntity();
        e.aggregateType = event.aggregateType();
        e.aggregateId = event.aggregateId();
        e.eventType = event.eventType();
        e.payload = event.payload();
        e.createdAt = Instant.now();
        return e;
    }
}
