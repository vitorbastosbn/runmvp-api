package com.runmvp.session.adapter.out.persistence;

import com.runmvp.session.domain.LocationPoint;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "location_points")
class LocationPointJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "participant_id", nullable = false) private Long participantId;
    @Column(nullable = false) private int sequence;
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    @Column(name = "accuracy_meters") private Double accuracyMeters;
    @Column(name = "speed_mps") private Double speedMps;
    @Column(name = "is_mocked", nullable = false) private boolean isMocked;
    @Column(name = "captured_at", nullable = false) private Instant capturedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    static LocationPointJpaEntity fromDomain(LocationPoint p) {
        LocationPointJpaEntity e = new LocationPointJpaEntity();
        e.participantId = p.getParticipantId(); e.sequence = p.getSequence();
        e.latitude = p.getLatitude(); e.longitude = p.getLongitude();
        e.accuracyMeters = p.getAccuracyMeters(); e.speedMps = p.getSpeedMps();
        e.isMocked = p.isMocked(); e.capturedAt = p.getCapturedAt();
        e.createdAt = p.getCreatedAt();
        return e;
    }
}
