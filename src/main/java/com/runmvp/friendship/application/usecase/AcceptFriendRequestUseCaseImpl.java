package com.runmvp.friendship.application.usecase;

import com.runmvp.friendship.application.port.in.AcceptFriendRequestUseCase;
import com.runmvp.friendship.application.port.out.FriendshipRepository;
import com.runmvp.friendship.domain.Friendship;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcceptFriendRequestUseCaseImpl implements AcceptFriendRequestUseCase {
    private final FriendshipRepository repo;

    public AcceptFriendRequestUseCaseImpl(FriendshipRepository repo) { this.repo = repo; }

    @Override
    @Transactional
    public void execute(Long requestId, Long acceptorId) {
        Friendship f = repo.findById(requestId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));
        f.accept(acceptorId);
        repo.save(f);
    }
}
