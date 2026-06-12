package com.runmvp.session.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runmvp.session.application.port.in.FinishSessionUseCase;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.shared.outbox.OutboxEvent;
import com.runmvp.shared.outbox.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FinishSessionUseCaseImpl implements FinishSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper mapper;

    public FinishSessionUseCaseImpl(SessionRepository sessionRepository,
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
    public void execute(Command command) {
        sessionRepository.findById(command.sessionId())
            .filter(s -> s.isActive())
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_ACTIVE));

        SessionParticipant participant = participantRepository
            .findBySessionIdAndUserId(command.sessionId(), command.userId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_SESSION_PARTICIPANT));

        List<SessionParticipant> allParticipants =
            participantRepository.findBySessionId(command.sessionId());

        long finishedCount = allParticipants.stream()
            .filter(p -> p.getStatus() == SessionParticipant.Status.FINISHED)
            .count();

        int position = (int) finishedCount + 1;
        participant.finish(position, command.distanceMeters(), command.runningTimeSeconds());
        participantRepository.save(participant);

        var payload = mapper.createObjectNode()
            .put("sessionId", command.sessionId())
            .put("userId", command.userId())
            .put("position", position)
            .put("distanceMeters", command.distanceMeters())
            .put("runningTimeSeconds", command.runningTimeSeconds());
        outboxRepository.publish(OutboxEvent.of("RunningSession", command.sessionId(),
            "participant.finished", payload));

        long remainingActive = allParticipants.stream()
            .filter(p -> !p.getId().equals(participant.getId()))
            .filter(SessionParticipant::isActive)
            .count();

        if (remainingActive == 0) {
            var sessionPayload = mapper.createObjectNode().put("sessionId", command.sessionId());
            outboxRepository.publish(OutboxEvent.of("RunningSession", command.sessionId(),
                "session.all_finished", sessionPayload));
        }
    }
}
