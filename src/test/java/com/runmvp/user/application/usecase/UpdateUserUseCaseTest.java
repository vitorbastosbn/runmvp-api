package com.runmvp.user.application.usecase;

import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.user.application.port.in.UpdateUserUseCase;
import com.runmvp.user.application.port.out.UserRepository;
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
class UpdateUserUseCaseTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UpdateUserUseCaseImpl useCase;

    @Test
    void execute_updatesNameAndAvatar() {
        User user = User.create("sub","Old","e@x.com",null,"CODE1234");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        useCase.execute(new UpdateUserUseCase.Command(1L, "New Name", "https://new.img/a.jpg"));

        verify(userRepository).save(argThat(u ->
            "New Name".equals(u.getName()) && "https://new.img/a.jpg".equals(u.getAvatarUrl())
        ));
    }

    @Test
    void execute_unknownUser_throwsUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            useCase.execute(new UpdateUserUseCase.Command(99L, "Name", null)))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
