package com.runmvp.user.application.port.in;

import com.runmvp.user.application.port.out.UserStatisticsRepository.Statistics;

public interface GetUserStatisticsUseCase {
    Statistics execute(Long targetUserId, Long requesterId);
}
