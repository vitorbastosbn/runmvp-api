package com.runmvp.user.application.port.in;

public interface UpdateUserUseCase {
    record Command(Long userId, String name, String avatarUrl) {}
    void execute(Command command);
}
