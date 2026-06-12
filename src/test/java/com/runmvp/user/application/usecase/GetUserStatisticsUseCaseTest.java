package com.runmvp.user.application.usecase;

import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.user.application.port.out.UserRepository;
import com.runmvp.user.application.port.out.UserStatisticsRepository;
import com.runmvp.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserStatisticsUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private UserStatisticsRepository statisticsRepository;
    @InjectMocks private GetUserStatisticsUseCaseImpl useCase;

    @Test
    void execute_returnsStatisticsFromRepository() {
        User user = User.create("sub","Name","e@x.com",null,"CODE1234");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        var stats = new UserStatisticsRepository.Statistics(
            1L, 10, 8, 50_000L, 18_000L, 360, 10_000, 3, 1, 2);
        when(statisticsRepository.findByUserId(1L)).thenReturn(Optional.of(stats));

        var result = useCase.execute(1L, 2L);

        assertThat(result).isEqualTo(stats);
    }

    @Test
    void execute_noStatisticsYet_returnsZeroedStatistics() {
        User user = User.create("sub","Name","e@x.com",null,"CODE1234");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(statisticsRepository.findByUserId(1L)).thenReturn(Optional.empty());

        var result = useCase.execute(1L, 1L);

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.totalSessions()).isZero();
        assertThat(result.averagePaceSecPerKm()).isNull();
    }

    @Test
    void execute_unknownUser_throwsUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, 1L))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
