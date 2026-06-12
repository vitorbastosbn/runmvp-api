package com.runmvp.friendship.application.usecase;

import com.runmvp.friendship.application.port.in.ListFriendsUseCase;
import com.runmvp.friendship.application.port.out.FriendshipRepository;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.user.application.port.out.UserRepository;
import com.runmvp.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListFriendsUseCaseImpl implements ListFriendsUseCase {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public ListFriendsUseCaseImpl(FriendshipRepository friendshipRepository,
                                  UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> execute(Long userId, Pageable pageable) {
        return friendshipRepository.findAcceptedByUserId(userId, pageable)
            .map(f -> {
                Long friendId = f.getRequesterId().equals(userId)
                    ? f.getRecipientId()
                    : f.getRequesterId();
                return userRepository.findById(friendId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            });
    }
}
