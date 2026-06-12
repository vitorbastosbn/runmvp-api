package com.runmvp.session.application.port.out;

import com.runmvp.session.domain.RunningSession;
import java.util.Optional;

public interface SessionRepository {
    RunningSession save(RunningSession session);
    Optional<RunningSession> findById(Long id);
}
