package com.runmvp.session.adapter.in.web;

import com.runmvp.session.application.port.in.GetSessionResultUseCase;
import java.util.List;

public record SessionResultResponse(
    Long sessionId, String sessionStatus, String mode,
    Long targetDistanceMeters, String startedAt, String finishedAt,
    List<ParticipantDto> participants
) {
    public record ParticipantDto(
        Long userId, String name, String avatarUrl, String status,
        Integer finalPosition, Long distanceMeters, Long runningTimeSeconds
    ) {}

    public static SessionResultResponse from(GetSessionResultUseCase.Result result) {
        return new SessionResultResponse(
            result.sessionId(), result.sessionStatus().name(), result.mode().name(),
            result.targetDistanceMeters(), result.startedAt(), result.finishedAt(),
            result.participants().stream().map(p -> new ParticipantDto(
                p.userId(), p.name(), p.avatarUrl(), p.status().name(),
                p.finalPosition(), p.distanceMeters(), p.runningTimeSeconds()
            )).toList()
        );
    }
}
