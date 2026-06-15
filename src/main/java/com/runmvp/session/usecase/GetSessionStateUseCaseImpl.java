package com.runmvp.session.usecase;

import com.runmvp.session.application.port.in.GetSessionStateUseCase;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.RunningSession;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.user.application.port.out.UserRepository;
import com.runmvp.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetSessionStateUseCaseImpl implements GetSessionStateUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public GetSessionStateUseCaseImpl(SessionRepository sessionRepository,
                                      SessionParticipantRepository participantRepository,
                                      UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public State execute(Long sessionId, Long requestingUserId) {
        RunningSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        List<SessionParticipant> participants = participantRepository.findBySessionId(sessionId);

        boolean isMember = participants.stream()
            .anyMatch(p -> p.getUserId().equals(requestingUserId));
        if (!isMember) throw new BusinessException(ErrorCode.NOT_SESSION_PARTICIPANT);

        List<ParticipantState> states = participants.stream().map(p -> {
            User user = userRepository.findById(p.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            return new ParticipantState(
                p.getUserId(), user.getName(), user.getAvatarUrl(),
                p.getStatus(), p.getRole()
            );
        }).toList();

        return new State(
            session.getId(), session.getStatus(), session.getMode(),
            session.getTargetDistanceMeters(), states
        );
    }
}
