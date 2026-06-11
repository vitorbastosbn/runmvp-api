package com.runmvp.shared.outbox;

import org.springframework.stereotype.Repository;

@Repository
class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final OutboxEventJpaRepository jpa;

    OutboxEventRepositoryAdapter(OutboxEventJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void publish(OutboxEvent event) {
        jpa.save(OutboxEventJpaEntity.fromDomain(event));
    }
}
