package com.runmvp.session.domain;

import java.time.Instant;

public class SessionParticipant {

    public enum Status { INVITED, ACCEPTED, DECLINED, READY, RUNNING, FINISHED, ABANDONED }
    public enum Role   { CREATOR, GUEST }

    private Long id;
    private Long sessionId;
    private Long userId;
    private Status status;
    private Role role;
    private Integer finalPosition;
    private Long officialDistanceMeters;
    private Long runningTimeSeconds;
    private Instant createdAt;
    private Instant updatedAt;

    private SessionParticipant() {}

    public static SessionParticipant createInvite(Long sessionId, Long userId, Role role) {
        SessionParticipant p = new SessionParticipant();
        p.sessionId = sessionId;
        p.userId = userId;
        p.role = role;
        p.status = (role == Role.CREATOR) ? Status.ACCEPTED : Status.INVITED;
        p.createdAt = Instant.now();
        p.updatedAt = Instant.now();
        return p;
    }

    public static SessionParticipant reconstitute(Long id, Long sessionId, Long userId,
            Status status, Role role, Integer finalPosition, Long officialDistanceMeters,
            Long runningTimeSeconds, Instant createdAt, Instant updatedAt) {
        SessionParticipant p = new SessionParticipant();
        p.id = id; p.sessionId = sessionId; p.userId = userId;
        p.status = status; p.role = role; p.finalPosition = finalPosition;
        p.officialDistanceMeters = officialDistanceMeters;
        p.runningTimeSeconds = runningTimeSeconds;
        p.createdAt = createdAt; p.updatedAt = updatedAt;
        return p;
    }

    public void accept() {
        this.status = Status.ACCEPTED;
        this.updatedAt = Instant.now();
    }

    public void decline() {
        this.status = Status.DECLINED;
        this.updatedAt = Instant.now();
    }

    public void markReady() {
        this.status = Status.READY;
        this.updatedAt = Instant.now();
    }

    public void startRunning() {
        this.status = Status.RUNNING;
        this.updatedAt = Instant.now();
    }

    public void finish(int position, long distanceMeters, long timeSeconds) {
        this.status = Status.FINISHED;
        this.finalPosition = position;
        this.officialDistanceMeters = distanceMeters;
        this.runningTimeSeconds = timeSeconds;
        this.updatedAt = Instant.now();
    }

    public void abandon() {
        this.status = Status.ABANDONED;
        this.updatedAt = Instant.now();
    }

    public boolean isActive()   { return status == Status.ACCEPTED || status == Status.READY || status == Status.RUNNING; }

    public Long getId()                        { return id; }
    public Long getSessionId()                 { return sessionId; }
    public Long getUserId()                    { return userId; }
    public Status getStatus()                  { return status; }
    public Role getRole()                      { return role; }
    public Integer getFinalPosition()          { return finalPosition; }
    public Long getOfficialDistanceMeters()    { return officialDistanceMeters; }
    public Long getRunningTimeSeconds()        { return runningTimeSeconds; }
    public Instant getCreatedAt()              { return createdAt; }
    public Instant getUpdatedAt()              { return updatedAt; }
    public void setId(Long id)                 { this.id = id; }
}
