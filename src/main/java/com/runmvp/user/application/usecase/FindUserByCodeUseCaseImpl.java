package com.runmvp.user.application.usecase;

import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.user.application.port.in.FindUserByCodeUseCase;
import com.runmvp.user.application.port.out.UserRepository;
import com.runmvp.user.domain.User;
import org.springframework.stereotype.Service;

@Service
public class FindUserByCodeUseCaseImpl implements FindUserByCodeUseCase {
    private final UserRepository userRepository;

    public FindUserByCodeUseCaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User execute(String publicCode) {
        return userRepository.findByPublicCode(publicCode)
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
