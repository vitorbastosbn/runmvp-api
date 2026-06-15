package com.runmvp.session.adapter.in.web;

import com.runmvp.session.application.port.in.*;
import com.runmvp.session.domain.RunningSession;
import com.runmvp.shared.entitlement.EntitlementPort;
import com.runmvp.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final CreateSessionUseCase createSession;
    private final InviteToSessionUseCase inviteToSession;
    private final AcceptSessionInviteUseCase acceptInvite;
    private final DeclineSessionInviteUseCase declineInvite;
    private final MarkReadyUseCase markReady;
    private final StartSessionUseCase startSession;
    private final CancelSessionUseCase cancelSession;
    private final AbandonSessionUseCase abandonSession;
    private final SyncGpsActivityUseCase syncGpsActivity;
    private final FinishSessionUseCase finishSession;
    private final GetSessionStateUseCase getSessionState;
    private final GetSessionResultUseCase getSessionResult;
    private final EntitlementPort entitlementPort;

    public SessionController(CreateSessionUseCase createSession,
                             InviteToSessionUseCase inviteToSession,
                             AcceptSessionInviteUseCase acceptInvite,
                             DeclineSessionInviteUseCase declineInvite,
                             MarkReadyUseCase markReady,
                             StartSessionUseCase startSession,
                             CancelSessionUseCase cancelSession,
                             AbandonSessionUseCase abandonSession,
                             SyncGpsActivityUseCase syncGpsActivity,
                             FinishSessionUseCase finishSession,
                             GetSessionStateUseCase getSessionState,
                             GetSessionResultUseCase getSessionResult,
                             EntitlementPort entitlementPort) {
        this.createSession = createSession;
        this.inviteToSession = inviteToSession;
        this.acceptInvite = acceptInvite;
        this.declineInvite = declineInvite;
        this.markReady = markReady;
        this.startSession = startSession;
        this.cancelSession = cancelSession;
        this.abandonSession = abandonSession;
        this.syncGpsActivity = syncGpsActivity;
        this.finishSession = finishSession;
        this.getSessionState = getSessionState;
        this.getSessionResult = getSessionResult;
        this.entitlementPort = entitlementPort;
    }

    @PostMapping
    public ResponseEntity<CreateSessionResponse> create(
            @AuthenticationPrincipal AuthenticatedUser p,
            @Valid @RequestBody CreateSessionRequest req) {
        var result = createSession.execute(new CreateSessionUseCase.Command(
            p.userId(),
            RunningSession.Mode.valueOf(req.mode()),
            req.targetDistanceMeters(),
            req.scheduledAt() != null ? Instant.parse(req.scheduledAt()) : null,
            req.invitedUserIds() != null ? req.invitedUserIds() : List.of()
        ));
        return ResponseEntity.status(201).body(new CreateSessionResponse(result.sessionId()));
    }

    @PostMapping("/{id}/invite/{userId}")
    public ResponseEntity<Void> invite(
            @AuthenticationPrincipal AuthenticatedUser p,
            @PathVariable Long id,
            @PathVariable Long userId) {
        inviteToSession.execute(id, p.userId(), userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Void> accept(
            @AuthenticationPrincipal AuthenticatedUser p,
            @PathVariable Long id) {
        acceptInvite.execute(id, p.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<Void> decline(
            @AuthenticationPrincipal AuthenticatedUser p,
            @PathVariable Long id) {
        declineInvite.execute(id, p.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/ready")
    public ResponseEntity<Void> ready(
            @AuthenticationPrincipal AuthenticatedUser p,
            @PathVariable Long id) {
        markReady.execute(id, p.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> start(
            @AuthenticationPrincipal AuthenticatedUser p,
            @PathVariable Long id) {
        startSession.execute(id, p.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal AuthenticatedUser p,
            @PathVariable Long id) {
        cancelSession.execute(id, p.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/abandon")
    public ResponseEntity<Void> abandon(
            @AuthenticationPrincipal AuthenticatedUser p,
            @PathVariable Long id) {
        abandonSession.execute(id, p.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activity")
    public ResponseEntity<Void> activity(
            @AuthenticationPrincipal AuthenticatedUser p,
            @PathVariable Long id,
            @Valid @RequestBody ActivityRequest req) {
        syncGpsActivity.execute(new SyncGpsActivityUseCase.Command(
            id, p.userId(),
            req.points().stream()
                .map(pt -> new SyncGpsActivityUseCase.GpsPoint(
                    pt.sequence(), pt.latitude(), pt.longitude(),
                    pt.accuracyMeters(), pt.speedMps(), pt.isMocked(), pt.capturedAt()))
                .toList()
        ));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<Void> finish(
            @AuthenticationPrincipal AuthenticatedUser p,
            @PathVariable Long id,
            @Valid @RequestBody FinishRequest req) {
        finishSession.execute(new FinishSessionUseCase.Command(
            id, p.userId(), req.distanceMeters(), req.runningTimeSeconds()));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionStateResponse> state(
            @AuthenticationPrincipal AuthenticatedUser p,
            @PathVariable Long id) {
        return ResponseEntity.ok(SessionStateResponse.from(getSessionState.execute(id, p.userId())));
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<SessionResultResponse> result(
            @AuthenticationPrincipal AuthenticatedUser p,
            @PathVariable Long id) {
        var entitlement = entitlementPort.getEffectiveEntitlement(p.userId());
        var result = getSessionResult.execute(id, p.userId(), entitlement);
        return ResponseEntity.ok(SessionResultResponse.from(result));
    }

    record CreateSessionRequest(
        @NotNull String mode,
        Long targetDistanceMeters,
        String scheduledAt,
        List<Long> invitedUserIds
    ) {}

    record CreateSessionResponse(Long sessionId) {}

    record FinishRequest(long distanceMeters, long runningTimeSeconds) {}
}
