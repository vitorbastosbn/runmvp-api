package com.runmvp.session.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runmvp.session.application.port.in.FinishSessionUseCase;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.RunningSession;
import com.runmvp.session.domain.SessionParticipant;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinishSessionUseCaseTest {

    @Mock SessionRepository sessionRepository;
    @Mock SessionParticipantRepository participantRepository;
    @Mock OutboxEventRepository outboxRepository;
    @Spy ObjectMapper mapper = new ObjectMapper();
    @InjectMocks FinishSessionUseCaseImpl useCase;

    private RunningSession activeSession() {
        return RunningSession.reconstitute(1L, 1L, RunningSession.Status.ACTIVE,
            RunningSession.Mode.COMPETITIVE, 5000L, null, Instant.now(), null,
            Instant.now(), Instant.now());
    }

    private SessionParticipant runningParticipant(Long id, Long userId) {
        return SessionParticipant.reconstitute(id, 1L, userId,
            SessionParticipant.Status.RUNNING, SessionParticipant.Role.CREATOR,
            null, null, null, Instant.now(), Instant.now());
    }

    @Test
    void execute_runningParticipant_marksFinished() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(activeSession()));
        SessionParticipant participant = runningParticipant(10L, 1L);
        when(participantRepository.findBySessionIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(participant));
        when(participantRepository.findBySessionId(1L)).thenReturn(List.of(participant));
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new FinishSessionUseCase.Command(1L, 1L, 4800L, 1500L));

        verify(participantRepository).save(argThat(p ->
            p.getStatus() == SessionParticipant.Status.FINISHED));
    }

    @Test
    void execute_sessionNotActive_throws() {
        RunningSession completed = RunningSession.reconstitute(1L, 1L,
            RunningSession.Status.COMPLETED, RunningSession.Mode.COMPETITIVE,
            5000L, null, Instant.now(), Instant.now(), Instant.now(), Instant.now());
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(completed));

        assertThatThrownBy(() ->
            useCase.execute(new FinishSessionUseCase.Command(1L, 1L, 4800L, 1500L)))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SESSION_NOT_ACTIVE);
    }
}
