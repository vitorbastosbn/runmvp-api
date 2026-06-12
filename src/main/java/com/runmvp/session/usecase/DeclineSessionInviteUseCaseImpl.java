package com.runmvp.session.usecase;

import com.runmvp.session.application.port.in.DeclineSessionInviteUseCase;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeclineSessionInviteUseCaseImpl implements DeclineSessionInviteUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;

    public DeclineSessionInviteUseCaseImpl(SessionRepository sessionRepository,
                                           SessionParticipantRepository participantRepository) {
        this.sessionRepository = sessionRepository;
        this.participantRepository = participantRepository;
    }

    @Override
    @Transactional
    public void execute(Long sessionId, Long userId) {
        sessionRepository.findById(sessionId)
            .filter(s -> !s.isFinished())
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        SessionParticipant participant = participantRepository
            .findBySessionIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_SESSION_PARTICIPANT));

        participant.decline();
        participantRepository.save(participant);
    }
}
