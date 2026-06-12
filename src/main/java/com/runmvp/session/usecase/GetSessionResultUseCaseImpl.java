package com.runmvp.session.usecase;

import com.runmvp.session.application.port.in.GetSessionResultUseCase;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.RunningSession;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.entitlement.EntitlementPort;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.user.application.port.out.UserRepository;
import com.runmvp.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetSessionResultUseCaseImpl implements GetSessionResultUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public GetSessionResultUseCaseImpl(SessionRepository sessionRepository,
                                       SessionParticipantRepository participantRepository,
                                       UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Result execute(Long sessionId, Long requestingUserId,
                          EntitlementPort.Entitlement entitlement) {
        RunningSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        List<SessionParticipant> participants =
            participantRepository.findBySessionId(sessionId);

        boolean isMember = participants.stream()
            .anyMatch(p -> p.getUserId().equals(requestingUserId));
        if (!isMember) throw new BusinessException(ErrorCode.NOT_SESSION_PARTICIPANT);

        boolean isFree = entitlement == EntitlementPort.Entitlement.FREE;

        List<ParticipantResult> results = participants.stream().map(p -> {
            User user = userRepository.findById(p.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            Integer position = p.getFinalPosition();
            if (isFree && !p.getUserId().equals(requestingUserId)) position = null;

            return new ParticipantResult(p.getUserId(), user.getName(), user.getAvatarUrl(),
                p.getStatus(), position, p.getOfficialDistanceMeters(), p.getRunningTimeSeconds());
        }).toList();

        return new Result(
            session.getId(), session.getStatus(), session.getMode(),
            session.getTargetDistanceMeters(),
            session.getStartedAt() != null ? session.getStartedAt().toString() : null,
            session.getFinishedAt() != null ? session.getFinishedAt().toString() : null,
            results
        );
    }
}
