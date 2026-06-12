package com.runmvp.session.adapter.out.persistence;

import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.RunningSession;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class SessionRepositoryAdapter implements SessionRepository {

    private final RunningSessionJpaRepository jpa;

    SessionRepositoryAdapter(RunningSessionJpaRepository jpa) { this.jpa = jpa; }

    @Override public RunningSession save(RunningSession s) {
        return jpa.save(RunningSessionJpaEntity.fromDomain(s)).toDomain();
    }

    @Override public Optional<RunningSession> findById(Long id) {
        return jpa.findById(id).map(RunningSessionJpaEntity::toDomain);
    }
}
