package com.runmvp.session.domain;

import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import java.time.Instant;

public class RunningSession {

    public enum Status { PENDING, ACTIVE, COMPLETED, ABANDONED }
    public enum Mode   { COMPETITIVE, COOPERATIVE }

    private Long id;
    private Long creatorId;
    private Status status;
    private Mode mode;
    private Long targetDistanceMeters;
    private Instant scheduledAt;
    private Instant startedAt;
    private Instant finishedAt;
    private Instant createdAt;
    private Instant updatedAt;

    private RunningSession() {}

    public static RunningSession create(Long creatorId, Mode mode,
                                        Long targetDistanceMeters, Instant scheduledAt) {
        if (targetDistanceMeters != null && targetDistanceMeters < 100) {
            throw new BusinessException(ErrorCode.INVALID_SESSION_DISTANCE);
        }
        RunningSession s = new RunningSession();
        s.creatorId = creatorId;
        s.mode = mode;
        s.status = Status.PENDING;
        s.targetDistanceMeters = targetDistanceMeters;
        s.scheduledAt = scheduledAt;
        s.createdAt = Instant.now();
        s.updatedAt = Instant.now();
        return s;
    }

    public static RunningSession reconstitute(Long id, Long creatorId, Status status, Mode mode,
            Long targetDistanceMeters, Instant scheduledAt, Instant startedAt,
            Instant finishedAt, Instant createdAt, Instant updatedAt) {
        RunningSession s = new RunningSession();
        s.id = id; s.creatorId = creatorId; s.status = status; s.mode = mode;
        s.targetDistanceMeters = targetDistanceMeters; s.scheduledAt = scheduledAt;
        s.startedAt = startedAt; s.finishedAt = finishedAt;
        s.createdAt = createdAt; s.updatedAt = updatedAt;
        return s;
    }

    public void start() {
        if (status != Status.PENDING) throw new BusinessException(ErrorCode.INVALID_SESSION_STATE);
        this.status = Status.ACTIVE;
        this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void complete() {
        if (status != Status.ACTIVE) throw new BusinessException(ErrorCode.INVALID_SESSION_STATE);
        this.status = Status.COMPLETED;
        this.finishedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void abandon() {
        if (status == Status.COMPLETED) throw new BusinessException(ErrorCode.INVALID_SESSION_STATE);
        this.status = Status.ABANDONED;
        this.finishedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isPending()   { return status == Status.PENDING; }
    public boolean isActive()    { return status == Status.ACTIVE; }
    public boolean isFinished()  { return status == Status.COMPLETED || status == Status.ABANDONED; }

    public Long getId()                     { return id; }
    public Long getCreatorId()              { return creatorId; }
    public Status getStatus()               { return status; }
    public Mode getMode()                   { return mode; }
    public Long getTargetDistanceMeters()   { return targetDistanceMeters; }
    public Instant getScheduledAt()         { return scheduledAt; }
    public Instant getStartedAt()           { return startedAt; }
    public Instant getFinishedAt()          { return finishedAt; }
    public Instant getCreatedAt()           { return createdAt; }
    public Instant getUpdatedAt()           { return updatedAt; }
    public void setId(Long id)              { this.id = id; }
}
