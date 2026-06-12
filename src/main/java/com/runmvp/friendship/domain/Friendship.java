package com.runmvp.friendship.domain;

import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import java.time.Instant;

public class Friendship {

    public enum Status { PENDING, ACCEPTED, REJECTED }

    private Long id;
    private Long requesterId;
    private Long recipientId;
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;

    private Friendship() {}

    public static Friendship request(Long requesterId, Long recipientId) {
        if (requesterId.equals(recipientId)) {
            throw new BusinessException(ErrorCode.CANNOT_FRIEND_SELF);
        }
        Friendship f = new Friendship();
        f.requesterId = requesterId;
        f.recipientId = recipientId;
        f.status = Status.PENDING;
        f.createdAt = Instant.now();
        f.updatedAt = Instant.now();
        return f;
    }

    public static Friendship reconstitute(Long id, Long requesterId, Long recipientId,
                                          Status status, Instant createdAt, Instant updatedAt) {
        Friendship f = new Friendship();
        f.id = id; f.requesterId = requesterId; f.recipientId = recipientId;
        f.status = status; f.createdAt = createdAt; f.updatedAt = updatedAt;
        return f;
    }

    public void accept(Long acceptorId) {
        if (!recipientId.equals(acceptorId)) throw new BusinessException(ErrorCode.NOT_REQUEST_RECIPIENT);
        if (status != Status.PENDING) throw new BusinessException(ErrorCode.INVALID_SESSION_STATE);
        this.status = Status.ACCEPTED;
        this.updatedAt = Instant.now();
    }

    public void reject(Long rejectorId) {
        if (!recipientId.equals(rejectorId)) throw new BusinessException(ErrorCode.NOT_REQUEST_RECIPIENT);
        this.status = Status.REJECTED;
        this.updatedAt = Instant.now();
    }

    public boolean isAccepted() { return status == Status.ACCEPTED; }

    public Long getId()           { return id; }
    public Long getRequesterId()  { return requesterId; }
    public Long getRecipientId()  { return recipientId; }
    public Status getStatus()     { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
