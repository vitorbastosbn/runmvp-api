package com.runmvp.session.application.port.in;

public interface FinishSessionUseCase {
    record Command(Long sessionId, Long userId,
                   long distanceMeters, long runningTimeSeconds) {}
    void execute(Command command);
}
