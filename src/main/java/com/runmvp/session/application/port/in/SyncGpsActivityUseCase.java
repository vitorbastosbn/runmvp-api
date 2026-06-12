package com.runmvp.session.application.port.in;

import java.time.Instant;
import java.util.List;

public interface SyncGpsActivityUseCase {

    record GpsPoint(
        int sequence,
        double latitude,
        double longitude,
        Double accuracyMeters,
        Double speedMps,
        boolean isMocked,
        Instant capturedAt
    ) {}

    record Command(Long sessionId, Long userId, List<GpsPoint> points) {}

    void execute(Command command);
}
