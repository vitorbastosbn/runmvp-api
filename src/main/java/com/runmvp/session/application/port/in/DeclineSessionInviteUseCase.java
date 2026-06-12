package com.runmvp.session.application.port.in;

public interface DeclineSessionInviteUseCase {
    void execute(Long sessionId, Long userId);
}
