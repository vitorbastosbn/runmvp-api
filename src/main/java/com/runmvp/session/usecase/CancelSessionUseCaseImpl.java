package com.runmvp.session.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runmvp.session.application.port.in.CancelSessionUseCase;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.RunningSession;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.shared.outbox.OutboxEvent;
import com.runmvp.shared.outbox.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelSessionUseCaseImpl implements CancelSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper mapper;

    public CancelSessionUseCaseImpl(SessionRepository sessionRepository,
                                    SessionParticipantRepository participantRepository,
                                    OutboxEventRepository outboxRepository,
                                    ObjectMapper mapper) {
        this.sessionRepository = sessionRepository;
        this.participantRepository = participantRepository;
        this.outboxRepository = outboxRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void execute(Long sessionId, Long requestingUserId) {
        RunningSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (!session.getCreatorId().equals(requestingUserId))
            throw new BusinessException(ErrorCode.NOT_SESSION_CREATOR);

        participantRepository.findBySessionId(sessionId)
            .forEach(p -> { p.abandon(); participantRepository.save(p); });

        session.abandon();
        sessionRepository.save(session);

        var payload = mapper.createObjectNode().put("sessionId", sessionId);
        outboxRepository.publish(OutboxEvent.of("RunningSession", sessionId, "session.cancelled", payload));
    }
}
