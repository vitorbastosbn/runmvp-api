package com.runmvp.session.usecase;

import com.runmvp.session.application.port.in.SyncGpsActivityUseCase;
import com.runmvp.session.application.port.out.LocationPointRepository;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.RunningSession;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncGpsActivityUseCaseTest {

    @Mock SessionRepository sessionRepository;
    @Mock SessionParticipantRepository participantRepository;
    @Mock LocationPointRepository locationPointRepository;
    @InjectMocks SyncGpsActivityUseCaseImpl useCase;

    private RunningSession activeSession() {
        return RunningSession.reconstitute(1L, 1L, RunningSession.Status.ACTIVE,
            RunningSession.Mode.COMPETITIVE, 5000L, null, Instant.now(), null,
            Instant.now(), Instant.now());
    }

    private SessionParticipant runningParticipant(Long userId) {
        return SessionParticipant.reconstitute(
            10L, 1L, userId, SessionParticipant.Status.RUNNING,
            SessionParticipant.Role.CREATOR, null, null, null,
            Instant.now(), Instant.now());
    }

    @Test
    void execute_validPoints_savesNonMocked() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(activeSession()));
        when(participantRepository.findBySessionIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(runningParticipant(1L)));
        when(locationPointRepository.findExistingSequences(eq(10L), any()))
            .thenReturn(Set.of());

        useCase.execute(new SyncGpsActivityUseCase.Command(1L, 1L, List.of(
            new SyncGpsActivityUseCase.GpsPoint(0, -23.5, -46.6, 5.0, 2.5, false, Instant.now()),
            new SyncGpsActivityUseCase.GpsPoint(1, -23.51, -46.61, 4.0, 2.6, false, Instant.now())
        )));

        verify(locationPointRepository).saveAll(argThat(list -> list.size() == 2));
    }

    @Test
    void execute_mockedPoints_filteredBeforeSave() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(activeSession()));
        when(participantRepository.findBySessionIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(runningParticipant(1L)));
        when(locationPointRepository.findExistingSequences(eq(10L), any()))
            .thenReturn(Set.of());

        useCase.execute(new SyncGpsActivityUseCase.Command(1L, 1L, List.of(
            new SyncGpsActivityUseCase.GpsPoint(0, -23.5, -46.6, 5.0, 2.5, true, Instant.now())
        )));

        verify(locationPointRepository).saveAll(argThat(List::isEmpty));
    }

    @Test
    void execute_duplicateSequences_idempotentIgnored() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(activeSession()));
        when(participantRepository.findBySessionIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(runningParticipant(1L)));
        when(locationPointRepository.findExistingSequences(eq(10L), any()))
            .thenReturn(Set.of(0));

        useCase.execute(new SyncGpsActivityUseCase.Command(1L, 1L, List.of(
            new SyncGpsActivityUseCase.GpsPoint(0, -23.5, -46.6, 5.0, 2.5, false, Instant.now()),
            new SyncGpsActivityUseCase.GpsPoint(1, -23.51, -46.61, 4.0, 2.6, false, Instant.now())
        )));

        verify(locationPointRepository).saveAll(argThat(list -> list.size() == 1));
    }

    @Test
    void execute_sessionNotActive_throws() {
        RunningSession pending = RunningSession.reconstitute(1L, 1L,
            RunningSession.Status.PENDING, RunningSession.Mode.COMPETITIVE,
            5000L, null, null, null, Instant.now(), Instant.now());
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() ->
            useCase.execute(new SyncGpsActivityUseCase.Command(1L, 1L, List.of())))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SESSION_NOT_ACTIVE);
    }

    @Test
    void execute_userNotParticipant_throws() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(activeSession()));
        when(participantRepository.findBySessionIdAndUserId(1L, 99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            useCase.execute(new SyncGpsActivityUseCase.Command(1L, 99L, List.of())))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.NOT_SESSION_PARTICIPANT);
    }
}
