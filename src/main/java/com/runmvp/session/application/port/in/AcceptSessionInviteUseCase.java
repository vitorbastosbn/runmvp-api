package com.runmvp.session.application.port.in;

public interface AcceptSessionInviteUseCase {
    void execute(Long sessionId, Long userId);
}
