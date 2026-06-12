package com.runmvp.friendship.application.port.in;

public interface RemoveFriendUseCase {
    void execute(Long userId, Long friendId);
}
