package com.runmvp.session.usecase;

import com.runmvp.session.application.port.in.SyncGpsActivityUseCase;
import com.runmvp.session.application.port.out.LocationPointRepository;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.LocationPoint;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SyncGpsActivityUseCaseImpl implements SyncGpsActivityUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final LocationPointRepository locationPointRepository;

    public SyncGpsActivityUseCaseImpl(SessionRepository sessionRepository,
                                      SessionParticipantRepository participantRepository,
                                      LocationPointRepository locationPointRepository) {
        this.sessionRepository = sessionRepository;
        this.participantRepository = participantRepository;
        this.locationPointRepository = locationPointRepository;
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

        if (command.points().isEmpty()) return;

        Set<Integer> incomingSequences = command.points().stream()
            .map(GpsPoint::sequence)
            .collect(Collectors.toSet());

        Set<Integer> existing = locationPointRepository
            .findExistingSequences(participant.getId(), incomingSequences);

        List<LocationPoint> toSave = command.points().stream()
            .filter(p -> !p.isMocked())
            .filter(p -> !existing.contains(p.sequence()))
            .map(p -> LocationPoint.create(
                participant.getId(), p.sequence(),
                p.latitude(), p.longitude(),
                p.accuracyMeters(), p.speedMps(),
                false, p.capturedAt()))
            .toList();

        locationPointRepository.saveAll(toSave);
    }
}
