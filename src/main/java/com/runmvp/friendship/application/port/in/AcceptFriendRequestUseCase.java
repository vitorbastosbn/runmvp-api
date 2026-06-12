package com.runmvp.friendship.application.port.in;

public interface AcceptFriendRequestUseCase {
    void execute(Long requestId, Long acceptorId);
}
