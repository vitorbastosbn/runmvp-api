package com.runmvp.session.application.port.in;

import com.runmvp.session.domain.RunningSession;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.entitlement.EntitlementPort;
import java.util.List;

public interface GetSessionResultUseCase {

    record ParticipantResult(
        Long userId, String name, String avatarUrl,
        SessionParticipant.Status status,
        Integer finalPosition, Long distanceMeters, Long runningTimeSeconds
    ) {}

    record Result(
        Long sessionId, RunningSession.Status sessionStatus,
        RunningSession.Mode mode, Long targetDistanceMeters,
        String startedAt, String finishedAt,
        List<ParticipantResult> participants
    ) {}

    Result execute(Long sessionId, Long requestingUserId,
                   EntitlementPort.Entitlement entitlement);
}
