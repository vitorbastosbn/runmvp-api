package com.runmvp.subscription.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SubscriptionJpaRepository extends JpaRepository<SubscriptionJpaEntity, Long> {
    Optional<SubscriptionJpaEntity> findByUserId(Long userId);
    Optional<SubscriptionJpaEntity> findByPurchaseToken(String purchaseToken);
}
