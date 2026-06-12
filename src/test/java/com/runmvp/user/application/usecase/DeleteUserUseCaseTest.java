package com.runmvp.user.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runmvp.shared.outbox.OutboxEventRepository;
import com.runmvp.user.application.port.out.UserRepository;
import com.runmvp.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private OutboxEventRepository outboxEventRepository;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks private DeleteUserUseCaseImpl useCase;

    @Test
    void execute_softDeletesUserAndPublishesOutboxEvent() {
        User user = User.create("sub","Name","e@x.com",null,"CODE1234");
        user.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        useCase.execute(5L);

        verify(userRepository).save(argThat(User::isDeleted));
        verify(outboxEventRepository).publish(argThat(e ->
            "user.deleted".equals(e.eventType()) && e.aggregateId().equals(5L)
        ));
    }
}
