package com.runmvp.friendship.application.port.in;

public interface RejectFriendRequestUseCase {
    void execute(Long requestId, Long rejectorId);
}
