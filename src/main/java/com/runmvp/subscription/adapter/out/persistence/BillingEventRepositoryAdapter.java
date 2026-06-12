package com.runmvp.subscription.adapter.out.persistence;

import com.runmvp.subscription.application.port.out.BillingEventRepository;
import org.springframework.stereotype.Repository;

@Repository
class BillingEventRepositoryAdapter implements BillingEventRepository {

    private final BillingEventJpaRepository jpa;

    BillingEventRepositoryAdapter(BillingEventJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public void save(Event event) {
        jpa.save(BillingEventJpaEntity.from(event));
    }
}
