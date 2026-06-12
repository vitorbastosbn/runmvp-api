package com.runmvp.subscription.adapter.out.persistence;

import com.runmvp.subscription.application.port.out.SubscriptionRepository;
import com.runmvp.subscription.domain.Subscription;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final SubscriptionJpaRepository jpa;

    SubscriptionRepositoryAdapter(SubscriptionJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public Subscription save(Subscription s) {
        return jpa.save(SubscriptionJpaEntity.fromDomain(s)).toDomain();
    }

    @Override
    public Optional<Subscription> findByUserId(Long userId) {
        return jpa.findByUserId(userId).map(SubscriptionJpaEntity::toDomain);
    }

    @Override
    public Optional<Subscription> findByPurchaseToken(String token) {
        return jpa.findByPurchaseToken(token).map(SubscriptionJpaEntity::toDomain);
    }
}
