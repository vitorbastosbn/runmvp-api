package com.runmvp.session.usecase;

import com.runmvp.session.application.port.in.GetSessionResultUseCase;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.RunningSession;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.entitlement.EntitlementPort;
import com.runmvp.user.application.port.out.UserRepository;
import com.runmvp.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetSessionResultUseCaseTest {

    @Mock SessionRepository sessionRepository;
    @Mock SessionParticipantRepository participantRepository;
    @Mock UserRepository userRepository;
    @InjectMocks GetSessionResultUseCaseImpl useCase;

    private User user(Long id, String name) {
        User u = User.create("sub" + id, name, name.toLowerCase() + "@x.com", null, "CODE" + id);
        u.setId(id);
        return u;
    }

    @Test
    void execute_completedSession_returnsParticipants() {
        RunningSession session = RunningSession.reconstitute(1L, 1L,
            RunningSession.Status.COMPLETED, RunningSession.Mode.COMPETITIVE,
            5000L, null, Instant.now(), Instant.now(), Instant.now(), Instant.now());
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        SessionParticipant p1 = SessionParticipant.reconstitute(1L, 1L, 1L,
            SessionParticipant.Status.FINISHED, SessionParticipant.Role.CREATOR,
            1, 5000L, 1200L, Instant.now(), Instant.now());
        when(participantRepository.findBySessionId(1L)).thenReturn(List.of(p1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "Alice")));

        GetSessionResultUseCase.Result result = useCase.execute(
            1L, 1L, EntitlementPort.Entitlement.PREMIUM_ACTIVE);

        assertThat(result.participants()).hasSize(1);
        assertThat(result.participants().get(0).finalPosition()).isEqualTo(1);
    }

    @Test
    void execute_freeUser_otherParticipantPositionHidden() {
        RunningSession session = RunningSession.reconstitute(1L, 1L,
            RunningSession.Status.COMPLETED, RunningSession.Mode.COMPETITIVE,
            5000L, null, Instant.now(), Instant.now(), Instant.now(), Instant.now());
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        SessionParticipant p1 = SessionParticipant.reconstitute(1L, 1L, 1L,
            SessionParticipant.Status.FINISHED, SessionParticipant.Role.CREATOR,
            1, 5000L, 1200L, Instant.now(), Instant.now());
        SessionParticipant p2 = SessionParticipant.reconstitute(2L, 1L, 2L,
            SessionParticipant.Status.FINISHED, SessionParticipant.Role.GUEST,
            2, 4900L, 1300L, Instant.now(), Instant.now());
        when(participantRepository.findBySessionId(1L)).thenReturn(List.of(p1, p2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "Alice")));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, "Bob")));

        GetSessionResultUseCase.Result result = useCase.execute(
            1L, 1L, EntitlementPort.Entitlement.FREE);

        var self  = result.participants().stream().filter(p -> p.userId().equals(1L)).findFirst().orElseThrow();
        var other = result.participants().stream().filter(p -> p.userId().equals(2L)).findFirst().orElseThrow();

        assertThat(self.finalPosition()).isEqualTo(1);
        assertThat(other.finalPosition()).isNull();
    }
}
