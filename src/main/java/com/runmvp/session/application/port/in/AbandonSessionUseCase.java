package com.runmvp.session.application.port.in;

public interface AbandonSessionUseCase {
    void execute(Long sessionId, Long userId);
}
