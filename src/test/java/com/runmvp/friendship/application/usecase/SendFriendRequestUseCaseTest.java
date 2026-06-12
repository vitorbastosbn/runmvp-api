package com.runmvp.friendship.application.usecase;

import com.runmvp.friendship.application.port.in.SendFriendRequestUseCase;
import com.runmvp.friendship.application.port.out.FriendshipRepository;
import com.runmvp.friendship.domain.Friendship;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.user.application.port.out.UserRepository;
import com.runmvp.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendFriendRequestUseCaseTest {

    @Mock FriendshipRepository friendshipRepository;
    @Mock UserRepository userRepository;
    @InjectMocks SendFriendRequestUseCaseImpl useCase;

    @Test
    void execute_validRequest_savesFriendship() {
        User recipient = User.create("sub-r","Rec","r@x.com",null,"RCODE123");
        recipient.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.findPendingBetween(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.findAcceptedBetween(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any())).thenAnswer(inv ->
            Friendship.reconstitute(10L, 1L, 2L, Friendship.Status.PENDING,
                Instant.now(), Instant.now()));

        useCase.execute(new SendFriendRequestUseCase.Command(1L, 2L));

        verify(friendshipRepository).save(any());
    }

    @Test
    void execute_toSelf_throwsCannotFriendSelf() {
        assertThatThrownBy(() ->
            useCase.execute(new SendFriendRequestUseCase.Command(1L, 1L)))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.CANNOT_FRIEND_SELF);
    }

    @Test
    void execute_alreadyFriends_throwsAlreadyExists() {
        User recipient = User.create("sub-r","Rec","r@x.com",null,"RCODE123");
        recipient.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.findAcceptedBetween(1L, 2L))
            .thenReturn(Optional.of(Friendship.reconstitute(
                1L, 1L, 2L, Friendship.Status.ACCEPTED,
                Instant.now(), Instant.now())));

        assertThatThrownBy(() ->
            useCase.execute(new SendFriendRequestUseCase.Command(1L, 2L)))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.FRIENDSHIP_ALREADY_EXISTS);
    }
}
