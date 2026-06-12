package com.runmvp.friendship.application.port.in;

import com.runmvp.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface ListFriendRequestsUseCase {
    record RequestEntry(Long id, User requester, Instant createdAt) {}
    Page<RequestEntry> execute(Long userId, Pageable pageable);
}
