package com.runmvp.session.usecase;

import com.runmvp.session.application.port.in.CreateSessionUseCase;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.application.port.out.SessionRepository;
import com.runmvp.session.domain.RunningSession;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.shared.entitlement.EntitlementPort;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class CreateSessionUseCaseImpl implements CreateSessionUseCase {

    private static final Set<Long> FREE_DISTANCES_METERS = Set.of(1000L, 3000L, 5000L, 10000L);

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final EntitlementPort entitlementPort;

    public CreateSessionUseCaseImpl(SessionRepository sessionRepository,
                                    SessionParticipantRepository participantRepository,
                                    EntitlementPort entitlementPort) {
        this.sessionRepository = sessionRepository;
        this.participantRepository = participantRepository;
        this.entitlementPort = entitlementPort;
    }

    @Override
    @Transactional
    public Result execute(Command command) {
        List<Long> invitedUserIds = command.invitedUserIds() != null
            ? command.invitedUserIds()
            : List.of();
        if (invitedUserIds.size() > 9) {
            throw new BusinessException(ErrorCode.INVALID_INVITEE_COUNT);
        }

        EntitlementPort.Entitlement entitlement =
            entitlementPort.getEffectiveEntitlement(command.creatorId());
        boolean isPremium = entitlement == EntitlementPort.Entitlement.PREMIUM_ACTIVE;
        boolean competitive = command.mode() == RunningSession.Mode.COMPETITIVE;
        boolean customDistance = command.targetDistanceMeters() != null
            && !FREE_DISTANCES_METERS.contains(command.targetDistanceMeters());
        if (!isPremium && (competitive || customDistance)) {
            throw new BusinessException(ErrorCode.PREMIUM_REQUIRED);
        }

        RunningSession session = RunningSession.create(
            command.creatorId(), command.mode(),
            command.targetDistanceMeters(), command.scheduledAt());
        RunningSession saved = sessionRepository.save(session);

        participantRepository.save(
            SessionParticipant.createInvite(saved.getId(), command.creatorId(),
                SessionParticipant.Role.CREATOR));

        for (Long inviteeId : invitedUserIds) {
            participantRepository.save(
                SessionParticipant.createInvite(saved.getId(), inviteeId,
                    SessionParticipant.Role.GUEST));
        }

        return new Result(saved.getId());
    }
}
