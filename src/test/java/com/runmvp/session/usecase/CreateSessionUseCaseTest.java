package com.runmvp.session.usecase;

import com.runmvp.session.application.port.in.CreateSessionUseCase;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.RunningSession;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.entitlement.EntitlementPort;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSessionUseCaseTest {

    @Mock SessionRepository sessionRepository;
    @Mock SessionParticipantRepository participantRepository;
    @Mock EntitlementPort entitlementPort;
    @InjectMocks CreateSessionUseCaseImpl useCase;

    @Test
    void execute_validCommand_createsSessionAndCreatorParticipant() {
        when(entitlementPort.getEffectiveEntitlement(1L))
            .thenReturn(EntitlementPort.Entitlement.FREE);
        RunningSession saved = RunningSession.reconstitute(
            1L, 1L, RunningSession.Status.PENDING, RunningSession.Mode.COOPERATIVE,
            5000L, null, null, null, java.time.Instant.now(), java.time.Instant.now());
        when(sessionRepository.save(any())).thenReturn(saved);

        SessionParticipant creatorPart = SessionParticipant.reconstitute(
            1L, 1L, 1L, SessionParticipant.Status.ACCEPTED,
            SessionParticipant.Role.CREATOR, null, null, null,
            java.time.Instant.now(), java.time.Instant.now());
        when(participantRepository.save(any())).thenReturn(creatorPart);

        CreateSessionUseCase.Result result = useCase.execute(
            new CreateSessionUseCase.Command(1L, RunningSession.Mode.COOPERATIVE, 5000L, null, List.of()));

        assertThat(result.sessionId()).isEqualTo(1L);
        verify(sessionRepository).save(any());
        verify(participantRepository, atLeastOnce()).save(any());
    }

    @Test
    void execute_withInvitees_savesParticipantPerInvitee() {
        when(entitlementPort.getEffectiveEntitlement(1L))
            .thenReturn(EntitlementPort.Entitlement.FREE);
        RunningSession saved = RunningSession.reconstitute(
            1L, 1L, RunningSession.Status.PENDING, RunningSession.Mode.COOPERATIVE,
            5000L, null, null, null, java.time.Instant.now(), java.time.Instant.now());
        when(sessionRepository.save(any())).thenReturn(saved);
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new CreateSessionUseCase.Command(
            1L, RunningSession.Mode.COOPERATIVE, 5000L, null, List.of(2L, 3L)));

        // 1 creator + 2 guests = 3 saves
        verify(participantRepository, times(3)).save(any());
    }

    @Test
    void execute_freeUserCreatingCompetitiveSession_throwsPremiumRequired() {
        when(entitlementPort.getEffectiveEntitlement(1L))
            .thenReturn(EntitlementPort.Entitlement.FREE);

        assertThatThrownBy(() -> useCase.execute(new CreateSessionUseCase.Command(
            1L, RunningSession.Mode.COMPETITIVE, 5000L, null, List.of())))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.PREMIUM_REQUIRED);

        verifyNoInteractions(sessionRepository, participantRepository);
    }

    @Test
    void execute_freeUserWithCustomDistance_throwsPremiumRequired() {
        when(entitlementPort.getEffectiveEntitlement(1L))
            .thenReturn(EntitlementPort.Entitlement.FREE);

        assertThatThrownBy(() -> useCase.execute(new CreateSessionUseCase.Command(
            1L, RunningSession.Mode.COOPERATIVE, 7500L, null, List.of())))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.PREMIUM_REQUIRED);

        verifyNoInteractions(sessionRepository, participantRepository);
    }

    @Test
    void execute_premiumUserWithCompetitiveCustomDistance_createsSession() {
        when(entitlementPort.getEffectiveEntitlement(1L))
            .thenReturn(EntitlementPort.Entitlement.PREMIUM_ACTIVE);
        RunningSession saved = RunningSession.reconstitute(
            1L, 1L, RunningSession.Status.PENDING, RunningSession.Mode.COMPETITIVE,
            7500L, null, null, null, java.time.Instant.now(), java.time.Instant.now());
        when(sessionRepository.save(any())).thenReturn(saved);
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateSessionUseCase.Result result = useCase.execute(
            new CreateSessionUseCase.Command(1L, RunningSession.Mode.COMPETITIVE, 7500L, null, List.of()));

        assertThat(result.sessionId()).isEqualTo(1L);
    }

    @Test
    void execute_withMoreThanNineInvitees_throwsInvalidInviteeCount() {
        assertThatThrownBy(() -> useCase.execute(new CreateSessionUseCase.Command(
            1L, RunningSession.Mode.COOPERATIVE, 5000L, null,
            List.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L))))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_INVITEE_COUNT);

        verifyNoInteractions(entitlementPort, sessionRepository, participantRepository);
    }
}
