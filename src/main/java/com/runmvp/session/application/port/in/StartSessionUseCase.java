package com.runmvp.session.application.port.in;

public interface StartSessionUseCase {
    void execute(Long sessionId, Long requestingUserId);
}
