package com.runmvp.user.application.port.in;

import com.runmvp.user.domain.User;

public interface FindUserByCodeUseCase {
    User execute(String publicCode);
}
