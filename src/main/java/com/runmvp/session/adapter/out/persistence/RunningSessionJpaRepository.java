package com.runmvp.session.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface RunningSessionJpaRepository extends JpaRepository<RunningSessionJpaEntity, Long> {}
