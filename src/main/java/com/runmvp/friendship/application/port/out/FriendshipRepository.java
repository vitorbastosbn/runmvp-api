package com.runmvp.friendship.application.port.out;

import com.runmvp.friendship.domain.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface FriendshipRepository {
    Friendship save(Friendship friendship);
    Optional<Friendship> findById(Long id);
    Optional<Friendship> findPendingBetween(Long userId1, Long userId2);
    Optional<Friendship> findAcceptedBetween(Long userId1, Long userId2);
    Page<Friendship> findAcceptedByUserId(Long userId, Pageable pageable);
    Page<Friendship> findPendingReceivedBy(Long userId, Pageable pageable);
    void delete(Friendship friendship);
}
