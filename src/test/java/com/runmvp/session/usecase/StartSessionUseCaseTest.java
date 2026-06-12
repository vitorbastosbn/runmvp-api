package com.runmvp.session.usecase;

import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.RunningSession;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.shared.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class StartSessionUseCaseTest {

    @Mock SessionRepository sessionRepository;
    @Mock SessionParticipantRepository participantRepository;
    @Mock OutboxEventRepository outboxRepository;
    @Spy ObjectMapper mapper = new ObjectMapper();
    @InjectMocks StartSessionUseCaseImpl useCase;

    private RunningSession pendingSession(Long creatorId) {
        return RunningSession.reconstitute(1L, creatorId, RunningSession.Status.PENDING,
            RunningSession.Mode.COOPERATIVE, 5000L, null, null, null, Instant.now(), Instant.now());
    }

    private SessionParticipant acceptedCreator(Long sessionId, Long userId) {
        return SessionParticipant.reconstitute(1L, sessionId, userId,
            SessionParticipant.Status.ACCEPTED, SessionParticipant.Role.CREATOR,
            null, null, null, Instant.now(), Instant.now());
    }

    @Test
    void execute_creatorAllParticipantsAccepted_startsSession() {
        RunningSession session = pendingSession(1L);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(participantRepository.findBySessionId(1L)).thenReturn(
            List.of(acceptedCreator(1L, 1L)));
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L, 1L);

        verify(sessionRepository).save(argThat(RunningSession::isActive));
        verify(outboxRepository).publish(any());
    }

    @Test
    void execute_notCreator_throwsUnauthorized() {
        RunningSession session = pendingSession(1L);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> useCase.execute(1L, 2L))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.NOT_SESSION_CREATOR);
    }

    @Test
    void execute_sessionAlreadyActive_throwsInvalidState() {
        RunningSession session = RunningSession.reconstitute(1L, 1L,
            RunningSession.Status.ACTIVE, RunningSession.Mode.COMPETITIVE,
            5000L, null, Instant.now(), null, Instant.now(), Instant.now());
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> useCase.execute(1L, 1L))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_SESSION_STATE);
    }
}
