package com.runmvp.session.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record ActivityRequest(@NotNull List<GpsPointDto> points) {

    public record GpsPointDto(
        int sequence,
        double latitude,
        double longitude,
        Double accuracyMeters,
        Double speedMps,
        boolean isMocked,
        @NotNull Instant capturedAt
    ) {}
}
