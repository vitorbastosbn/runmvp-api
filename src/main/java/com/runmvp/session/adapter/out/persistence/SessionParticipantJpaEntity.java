package com.runmvp.session.adapter.out.persistence;

import com.runmvp.session.domain.SessionParticipant;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "session_participants")
class SessionParticipantJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "session_id", nullable = false) private Long sessionId;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false, length = 20) private String role;
    @Column(name = "final_position") private Integer finalPosition;
    @Column(name = "official_distance_meters") private Long officialDistanceMeters;
    @Column(name = "running_time_seconds") private Long runningTimeSeconds;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    static SessionParticipantJpaEntity fromDomain(SessionParticipant p) {
        SessionParticipantJpaEntity e = new SessionParticipantJpaEntity();
        e.id = p.getId(); e.sessionId = p.getSessionId(); e.userId = p.getUserId();
        e.status = p.getStatus().name(); e.role = p.getRole().name();
        e.finalPosition = p.getFinalPosition();
        e.officialDistanceMeters = p.getOfficialDistanceMeters();
        e.runningTimeSeconds = p.getRunningTimeSeconds();
        e.createdAt = p.getCreatedAt(); e.updatedAt = p.getUpdatedAt();
        return e;
    }

    SessionParticipant toDomain() {
        return SessionParticipant.reconstitute(id, sessionId, userId,
            SessionParticipant.Status.valueOf(status), SessionParticipant.Role.valueOf(role),
            finalPosition, officialDistanceMeters, runningTimeSeconds, createdAt, updatedAt);
    }

    Long getId() { return id; }
    Long getSessionId() { return sessionId; }
    Long getUserId() { return userId; }
    String getStatus() { return status; }
}
