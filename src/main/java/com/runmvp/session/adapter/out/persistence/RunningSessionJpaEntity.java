package com.runmvp.session.adapter.out.persistence;

import com.runmvp.session.domain.RunningSession;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "running_sessions")
class RunningSessionJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "creator_id", nullable = false) private Long creatorId;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false, length = 20) private String mode;
    @Column(name = "target_distance_meters") private Long targetDistanceMeters;
    @Column(name = "scheduled_at") private Instant scheduledAt;
    @Column(name = "started_at") private Instant startedAt;
    @Column(name = "finished_at") private Instant finishedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    static RunningSessionJpaEntity fromDomain(RunningSession s) {
        RunningSessionJpaEntity e = new RunningSessionJpaEntity();
        e.id = s.getId(); e.creatorId = s.getCreatorId();
        e.status = s.getStatus().name(); e.mode = s.getMode().name();
        e.targetDistanceMeters = s.getTargetDistanceMeters();
        e.scheduledAt = s.getScheduledAt(); e.startedAt = s.getStartedAt();
        e.finishedAt = s.getFinishedAt(); e.createdAt = s.getCreatedAt();
        e.updatedAt = s.getUpdatedAt();
        return e;
    }

    RunningSession toDomain() {
        return RunningSession.reconstitute(id, creatorId,
            RunningSession.Status.valueOf(status), RunningSession.Mode.valueOf(mode),
            targetDistanceMeters, scheduledAt, startedAt, finishedAt, createdAt, updatedAt);
    }

    Long getId() { return id; }
}
