package com.runmvp.friendship.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

interface FriendshipJpaRepository extends JpaRepository<FriendshipJpaEntity, Long> {

    @Query("""
        SELECT f FROM FriendshipJpaEntity f
        WHERE ((f.requesterId = :u1 AND f.recipientId = :u2)
            OR (f.requesterId = :u2 AND f.recipientId = :u1))
          AND f.status = 'PENDING'
        """)
    Optional<FriendshipJpaEntity> findPendingBetween(Long u1, Long u2);

    @Query("""
        SELECT f FROM FriendshipJpaEntity f
        WHERE ((f.requesterId = :u1 AND f.recipientId = :u2)
            OR (f.requesterId = :u2 AND f.recipientId = :u1))
          AND f.status = 'ACCEPTED'
        """)
    Optional<FriendshipJpaEntity> findAcceptedBetween(Long u1, Long u2);

    Page<FriendshipJpaEntity> findByRequesterIdAndStatusOrRecipientIdAndStatus(
        Long req, String s1, Long rec, String s2, Pageable pageable);

    Page<FriendshipJpaEntity> findByRecipientIdAndStatus(Long recipientId, String status, Pageable pageable);
}
