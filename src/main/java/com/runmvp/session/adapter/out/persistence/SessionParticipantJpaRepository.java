package com.runmvp.session.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface SessionParticipantJpaRepository extends JpaRepository<SessionParticipantJpaEntity, Long> {
    Optional<SessionParticipantJpaEntity> findBySessionIdAndUserId(Long sessionId, Long userId);
    List<SessionParticipantJpaEntity> findBySessionId(Long sessionId);

    @Query("SELECT p FROM SessionParticipantJpaEntity p WHERE p.userId = :userId AND p.status = 'INVITED'")
    List<SessionParticipantJpaEntity> findInvitesByUserId(Long userId);
}
