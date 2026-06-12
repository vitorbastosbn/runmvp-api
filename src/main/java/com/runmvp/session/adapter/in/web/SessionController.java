package com.runmvp.session.adapter.in.web;

import com.runmvp.session.application.port.in.*;
import com.runmvp.session.domain.RunningSession;
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

    public SessionController(CreateSessionUseCase createSession,
                             InviteToSessionUseCase inviteToSession,
                             AcceptSessionInviteUseCase acceptInvite,
                             DeclineSessionInviteUseCase declineInvite) {
        this.createSession = createSession;
        this.inviteToSession = inviteToSession;
        this.acceptInvite = acceptInvite;
        this.declineInvite = declineInvite;
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

    record CreateSessionRequest(
        @NotNull String mode,
        Long targetDistanceMeters,
        String scheduledAt,
        List<Long> invitedUserIds
    ) {}

    record CreateSessionResponse(Long sessionId) {}
}
