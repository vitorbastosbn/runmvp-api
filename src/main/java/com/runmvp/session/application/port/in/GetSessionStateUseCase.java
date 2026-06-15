package com.runmvp.session.application.port.in;

import com.runmvp.session.domain.RunningSession;
import com.runmvp.session.domain.SessionParticipant;
import java.util.List;

public interface GetSessionStateUseCase {

    record ParticipantState(
        Long userId, String name, String avatarUrl,
        SessionParticipant.Status status, SessionParticipant.Role role
    ) {}

    record State(
        Long sessionId, RunningSession.Status status,
        RunningSession.Mode mode, Long targetDistanceMeters,
        List<ParticipantState> participants
    ) {}

    State execute(Long sessionId, Long requestingUserId);
}
