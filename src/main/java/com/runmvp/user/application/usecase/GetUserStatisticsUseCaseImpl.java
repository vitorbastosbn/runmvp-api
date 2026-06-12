package com.runmvp.user.application.usecase;

import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.user.application.port.in.GetUserStatisticsUseCase;
import com.runmvp.user.application.port.out.UserRepository;
import com.runmvp.user.application.port.out.UserStatisticsRepository;
import org.springframework.stereotype.Service;

@Service
public class GetUserStatisticsUseCaseImpl implements GetUserStatisticsUseCase {
    private final UserRepository userRepository;
    private final UserStatisticsRepository statisticsRepository;

    public GetUserStatisticsUseCaseImpl(UserRepository userRepository,
                                        UserStatisticsRepository statisticsRepository) {
        this.userRepository = userRepository;
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public UserStatisticsRepository.Statistics execute(Long targetUserId, Long requesterId) {
        userRepository.findById(targetUserId)
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return statisticsRepository.findByUserId(targetUserId)
            .orElseGet(() -> new UserStatisticsRepository.Statistics(
                targetUserId, 0, 0, 0L, 0L, null, 0, 0, 0, 0));
    }
}
