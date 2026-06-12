package com.runmvp.session.application.port.out;

import com.runmvp.session.domain.SessionParticipant;
import java.util.List;
import java.util.Optional;

public interface SessionParticipantRepository {
    SessionParticipant save(SessionParticipant participant);
    Optional<SessionParticipant> findById(Long id);
    Optional<SessionParticipant> findBySessionIdAndUserId(Long sessionId, Long userId);
    List<SessionParticipant> findBySessionId(Long sessionId);
    List<SessionParticipant> findActiveInvitesByUserId(Long userId);
}
