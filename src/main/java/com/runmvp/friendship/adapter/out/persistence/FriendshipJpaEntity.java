package com.runmvp.friendship.adapter.out.persistence;

import com.runmvp.friendship.domain.Friendship;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "friendships")
class FriendshipJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id", nullable = false) private Long requesterId;
    @Column(name = "recipient_id", nullable = false) private Long recipientId;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    static FriendshipJpaEntity fromDomain(Friendship f) {
        FriendshipJpaEntity e = new FriendshipJpaEntity();
        e.id = f.getId(); e.requesterId = f.getRequesterId();
        e.recipientId = f.getRecipientId(); e.status = f.getStatus().name();
        e.createdAt = f.getCreatedAt(); e.updatedAt = f.getUpdatedAt();
        return e;
    }

    Friendship toDomain() {
        return Friendship.reconstitute(id, requesterId, recipientId,
            Friendship.Status.valueOf(status), createdAt, updatedAt);
    }

    Long getId()          { return id; }
    Long getRequesterId() { return requesterId; }
    Long getRecipientId() { return recipientId; }
    String getStatus()    { return status; }
    Instant getCreatedAt() { return createdAt; }
}
