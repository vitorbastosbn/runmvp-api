package com.runmvp.session.application.port.in;

public interface InviteToSessionUseCase {
    void execute(Long sessionId, Long inviterId, Long inviteeId);
}
