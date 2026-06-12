package com.runmvp.session.adapter.out.persistence;

import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.domain.SessionParticipant;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class SessionParticipantRepositoryAdapter implements SessionParticipantRepository {

    private final SessionParticipantJpaRepository jpa;

    SessionParticipantRepositoryAdapter(SessionParticipantJpaRepository jpa) { this.jpa = jpa; }

    @Override public SessionParticipant save(SessionParticipant p) {
        return jpa.save(SessionParticipantJpaEntity.fromDomain(p)).toDomain();
    }

    @Override public Optional<SessionParticipant> findById(Long id) {
        return jpa.findById(id).map(SessionParticipantJpaEntity::toDomain);
    }

    @Override public Optional<SessionParticipant> findBySessionIdAndUserId(Long sid, Long uid) {
        return jpa.findBySessionIdAndUserId(sid, uid).map(SessionParticipantJpaEntity::toDomain);
    }

    @Override public List<SessionParticipant> findBySessionId(Long sid) {
        return jpa.findBySessionId(sid).stream().map(SessionParticipantJpaEntity::toDomain).toList();
    }

    @Override public List<SessionParticipant> findActiveInvitesByUserId(Long userId) {
        return jpa.findInvitesByUserId(userId).stream().map(SessionParticipantJpaEntity::toDomain).toList();
    }
}
