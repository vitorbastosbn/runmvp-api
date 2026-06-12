package com.runmvp.session.application.port.in;

import com.runmvp.session.domain.RunningSession;
import java.time.Instant;
import java.util.List;

public interface CreateSessionUseCase {
    record Command(Long creatorId, RunningSession.Mode mode,
                   Long targetDistanceMeters, Instant scheduledAt,
                   List<Long> invitedUserIds) {}
    record Result(Long sessionId) {}
    Result execute(Command command);
}
