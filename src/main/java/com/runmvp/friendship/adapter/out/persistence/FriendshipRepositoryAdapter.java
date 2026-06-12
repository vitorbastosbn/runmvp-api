package com.runmvp.friendship.adapter.out.persistence;

import com.runmvp.friendship.application.port.out.FriendshipRepository;
import com.runmvp.friendship.domain.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
class FriendshipRepositoryAdapter implements FriendshipRepository {

    private final FriendshipJpaRepository jpa;

    FriendshipRepositoryAdapter(FriendshipJpaRepository jpa) { this.jpa = jpa; }

    @Override public Friendship save(Friendship f) {
        return jpa.save(FriendshipJpaEntity.fromDomain(f)).toDomain();
    }

    @Override public Optional<Friendship> findById(Long id) {
        return jpa.findById(id).map(FriendshipJpaEntity::toDomain);
    }

    @Override public Optional<Friendship> findPendingBetween(Long u1, Long u2) {
        return jpa.findPendingBetween(u1, u2).map(FriendshipJpaEntity::toDomain);
    }

    @Override public Optional<Friendship> findAcceptedBetween(Long u1, Long u2) {
        return jpa.findAcceptedBetween(u1, u2).map(FriendshipJpaEntity::toDomain);
    }

    @Override public Page<Friendship> findAcceptedByUserId(Long userId, Pageable pageable) {
        return jpa.findByRequesterIdAndStatusOrRecipientIdAndStatus(
            userId, "ACCEPTED", userId, "ACCEPTED", pageable)
            .map(FriendshipJpaEntity::toDomain);
    }

    @Override public Page<Friendship> findPendingReceivedBy(Long userId, Pageable pageable) {
        return jpa.findByRecipientIdAndStatus(userId, "PENDING", pageable)
            .map(FriendshipJpaEntity::toDomain);
    }

    @Override public void delete(Friendship friendship) {
        jpa.deleteById(friendship.getId());
    }
}
