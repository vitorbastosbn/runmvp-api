package com.runmvp.session.adapter.in.web;

import com.runmvp.session.application.port.in.GetSessionStateUseCase;
import java.util.List;

public record SessionStateResponse(
    Long sessionId, String status, String mode,
    Long targetDistanceMeters,
    List<ParticipantDto> participants
) {
    public record ParticipantDto(
        Long userId, String name, String avatarUrl, String status, String role
    ) {}

    public static SessionStateResponse from(GetSessionStateUseCase.State state) {
        return new SessionStateResponse(
            state.sessionId(), state.status().name(), state.mode().name(),
            state.targetDistanceMeters(),
            state.participants().stream().map(p -> new ParticipantDto(
                p.userId(), p.name(), p.avatarUrl(), p.status().name(), p.role().name()
            )).toList()
        );
    }
}
