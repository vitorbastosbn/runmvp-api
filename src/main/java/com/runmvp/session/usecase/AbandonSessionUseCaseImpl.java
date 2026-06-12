package com.runmvp.session.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runmvp.session.application.port.in.AbandonSessionUseCase;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.shared.outbox.OutboxEvent;
import com.runmvp.shared.outbox.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AbandonSessionUseCaseImpl implements AbandonSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper mapper;

    public AbandonSessionUseCaseImpl(SessionRepository sessionRepository,
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
    public void execute(Long sessionId, Long userId) {
        sessionRepository.findById(sessionId)
            .filter(s -> s.isActive())
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        var participant = participantRepository
            .findBySessionIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_SESSION_PARTICIPANT));

        participant.abandon();
        participantRepository.save(participant);

        var payload = mapper.createObjectNode()
            .put("sessionId", sessionId)
            .put("userId", userId);
        outboxRepository.publish(OutboxEvent.of("RunningSession", sessionId, "participant.abandoned", payload));
    }
}
