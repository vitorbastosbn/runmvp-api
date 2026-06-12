package com.runmvp.session.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.RunningSession;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.shared.outbox.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelSessionUseCaseTest {

    @Mock SessionRepository sessionRepository;
    @Mock SessionParticipantRepository participantRepository;
    @Mock OutboxEventRepository outboxRepository;
    @Spy ObjectMapper mapper = new ObjectMapper();
    @InjectMocks CancelSessionUseCaseImpl useCase;

    @Test
    void execute_creatorCancels_pendingSession_savesAbandoned() {
        RunningSession session = RunningSession.reconstitute(1L, 1L,
            RunningSession.Status.PENDING, RunningSession.Mode.COMPETITIVE,
            5000L, null, null, null, Instant.now(), Instant.now());
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantRepository.findBySessionId(1L)).thenReturn(List.of());

        useCase.execute(1L, 1L);

        verify(sessionRepository).save(argThat(s -> s.getStatus() == RunningSession.Status.ABANDONED));
    }

    @Test
    void execute_nonCreator_throwsUnauthorized() {
        RunningSession session = RunningSession.reconstitute(1L, 1L,
            RunningSession.Status.PENDING, RunningSession.Mode.COMPETITIVE,
            5000L, null, null, null, Instant.now(), Instant.now());
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> useCase.execute(1L, 2L))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.NOT_SESSION_CREATOR);
    }
}
