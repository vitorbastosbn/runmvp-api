package com.runmvp.friendship.application.usecase;

import com.runmvp.friendship.application.port.in.RemoveFriendUseCase;
import com.runmvp.friendship.application.port.out.FriendshipRepository;
import com.runmvp.friendship.domain.Friendship;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveFriendUseCaseImpl implements RemoveFriendUseCase {
    private final FriendshipRepository repo;

    public RemoveFriendUseCaseImpl(FriendshipRepository repo) { this.repo = repo; }

    @Override
    @Transactional
    public void execute(Long userId, Long friendId) {
        Friendship f = repo.findAcceptedBetween(userId, friendId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FRIENDSHIP_NOT_FOUND));
        repo.delete(f);
    }
}
