package com.runmvp.session.application.port.in;

public interface CancelSessionUseCase {
    void execute(Long sessionId, Long requestingUserId);
}
