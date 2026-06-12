package com.runmvp.friendship.application.port.in;

import com.runmvp.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListFriendsUseCase {
    Page<User> execute(Long userId, Pageable pageable);
}
