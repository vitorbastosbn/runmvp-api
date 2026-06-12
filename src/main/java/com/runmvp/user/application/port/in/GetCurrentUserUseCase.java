package com.runmvp.user.application.port.in;

import com.runmvp.shared.entitlement.EntitlementPort.Entitlement;
import com.runmvp.user.domain.User;

public interface GetCurrentUserUseCase {
    record Result(User user, Entitlement entitlement) {}
    Result execute(Long userId);
}
