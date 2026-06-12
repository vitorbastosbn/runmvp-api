package com.runmvp.session.application.port.in;

public interface MarkReadyUseCase {
    void execute(Long sessionId, Long userId);
}
