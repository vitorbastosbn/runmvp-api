package com.runmvp.session.domain;

import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import java.time.Instant;

public class LocationPoint {

    private Long id;
    private Long participantId;
    private int sequence;
    private double latitude;
    private double longitude;
    private Double accuracyMeters;
    private Double speedMps;
    private boolean isMocked;
    private Instant capturedAt;
    private Instant createdAt;

    private LocationPoint() {}

    public static LocationPoint create(Long participantId, int sequence,
                                       double latitude, double longitude,
                                       Double accuracyMeters, Double speedMps,
                                       boolean isMocked, Instant capturedAt) {
        if (latitude < -90 || latitude > 90)
            throw new BusinessException(ErrorCode.INVALID_GPS_COORDINATES);
        if (longitude < -180 || longitude > 180)
            throw new BusinessException(ErrorCode.INVALID_GPS_COORDINATES);
        if (sequence < 0)
            throw new BusinessException(ErrorCode.INVALID_GPS_SEQUENCE);

        LocationPoint p = new LocationPoint();
        p.participantId = participantId; p.sequence = sequence;
        p.latitude = latitude; p.longitude = longitude;
        p.accuracyMeters = accuracyMeters; p.speedMps = speedMps;
        p.isMocked = isMocked; p.capturedAt = capturedAt;
        p.createdAt = Instant.now();
        return p;
    }

    public static LocationPoint reconstitute(Long id, Long participantId, int sequence,
            double latitude, double longitude, Double accuracyMeters, Double speedMps,
            boolean isMocked, Instant capturedAt, Instant createdAt) {
        LocationPoint p = new LocationPoint();
        p.id = id; p.participantId = participantId; p.sequence = sequence;
        p.latitude = latitude; p.longitude = longitude;
        p.accuracyMeters = accuracyMeters; p.speedMps = speedMps;
        p.isMocked = isMocked; p.capturedAt = capturedAt; p.createdAt = createdAt;
        return p;
    }

    public boolean isMocked()         { return isMocked; }
    public Long getId()               { return id; }
    public Long getParticipantId()    { return participantId; }
    public int getSequence()          { return sequence; }
    public double getLatitude()       { return latitude; }
    public double getLongitude()      { return longitude; }
    public Double getAccuracyMeters() { return accuracyMeters; }
    public Double getSpeedMps()       { return speedMps; }
    public Instant getCapturedAt()    { return capturedAt; }
    public Instant getCreatedAt()     { return createdAt; }
}
