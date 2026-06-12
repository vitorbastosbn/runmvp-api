package com.runmvp.friendship.application.port.in;

public interface SendFriendRequestUseCase {
    record Command(Long requesterId, Long recipientId) {}
    record Result(Long id) {}
    Result execute(Command command);
}
