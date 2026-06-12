package com.runmvp.session.usecase;

import com.runmvp.session.application.port.in.InviteToSessionUseCase;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InviteToSessionUseCaseImpl implements InviteToSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;

    public InviteToSessionUseCaseImpl(SessionRepository sessionRepository,
                                      SessionParticipantRepository participantRepository) {
        this.sessionRepository = sessionRepository;
        this.participantRepository = participantRepository;
    }

    @Override
    @Transactional
    public void execute(Long sessionId, Long inviterId, Long inviteeId) {
        sessionRepository.findById(sessionId)
            .filter(s -> s.isPending() && s.getCreatorId().equals(inviterId))
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (participantRepository.findBySessionIdAndUserId(sessionId, inviteeId).isPresent())
            throw new BusinessException(ErrorCode.ALREADY_SESSION_PARTICIPANT);

        participantRepository.save(
            SessionParticipant.createInvite(sessionId, inviteeId, SessionParticipant.Role.GUEST));
    }
}
