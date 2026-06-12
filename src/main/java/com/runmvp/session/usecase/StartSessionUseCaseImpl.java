package com.runmvp.session.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runmvp.session.application.port.in.StartSessionUseCase;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.RunningSession;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.shared.outbox.OutboxEvent;
import com.runmvp.shared.outbox.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StartSessionUseCaseImpl implements StartSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper mapper;

    public StartSessionUseCaseImpl(SessionRepository sessionRepository,
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

        if (!session.isPending())
            throw new BusinessException(ErrorCode.INVALID_SESSION_STATE);

        List<SessionParticipant> participants = participantRepository.findBySessionId(sessionId);
        participants.forEach(SessionParticipant::startRunning);
        participants.forEach(participantRepository::save);

        session.start();
        sessionRepository.save(session);

        var payload = mapper.createObjectNode()
            .put("sessionId", sessionId)
            .put("startedAt", session.getStartedAt().toString());
        outboxRepository.publish(OutboxEvent.of("RunningSession", sessionId, "session.started", payload));
    }
}
