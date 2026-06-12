package com.runmvp.user.application.usecase;

import com.runmvp.shared.entitlement.EntitlementPort;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.user.application.port.in.GetCurrentUserUseCase;
import com.runmvp.user.application.port.out.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentUserUseCaseImpl implements GetCurrentUserUseCase {
    private final UserRepository userRepository;
    private final EntitlementPort entitlementPort;

    public GetCurrentUserUseCaseImpl(UserRepository userRepository, EntitlementPort entitlementPort) {
        this.userRepository = userRepository;
        this.entitlementPort = entitlementPort;
    }

    @Override
    public Result execute(Long userId) {
        var user = userRepository.findById(userId)
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new Result(user, entitlementPort.getEffectiveEntitlement(userId));
    }
}
