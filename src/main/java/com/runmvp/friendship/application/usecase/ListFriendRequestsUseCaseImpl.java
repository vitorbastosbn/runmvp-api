package com.runmvp.friendship.application.usecase;

import com.runmvp.friendship.application.port.in.ListFriendRequestsUseCase;
import com.runmvp.friendship.application.port.out.FriendshipRepository;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.user.application.port.out.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListFriendRequestsUseCaseImpl implements ListFriendRequestsUseCase {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public ListFriendRequestsUseCaseImpl(FriendshipRepository friendshipRepository,
                                         UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RequestEntry> execute(Long userId, Pageable pageable) {
        return friendshipRepository.findPendingReceivedBy(userId, pageable)
            .map(f -> new RequestEntry(
                f.getId(),
                userRepository.findById(f.getRequesterId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)),
                f.getCreatedAt()
            ));
    }
}
