package com.runmvp.subscription.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface BillingEventJpaRepository extends JpaRepository<BillingEventJpaEntity, Long> {}
