package com.runmvp.friendship.application.usecase;

import com.runmvp.friendship.application.port.in.SendFriendRequestUseCase;
import com.runmvp.friendship.application.port.out.FriendshipRepository;
import com.runmvp.friendship.domain.Friendship;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.user.application.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SendFriendRequestUseCaseImpl implements SendFriendRequestUseCase {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public SendFriendRequestUseCaseImpl(FriendshipRepository friendshipRepository,
                                        UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Result execute(Command command) {
        if (command.requesterId().equals(command.recipientId()))
            throw new BusinessException(ErrorCode.CANNOT_FRIEND_SELF);

        userRepository.findById(command.recipientId())
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (friendshipRepository.findAcceptedBetween(
                command.requesterId(), command.recipientId()).isPresent())
            throw new BusinessException(ErrorCode.FRIENDSHIP_ALREADY_EXISTS);

        if (friendshipRepository.findPendingBetween(
                command.requesterId(), command.recipientId()).isPresent())
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_ALREADY_PENDING);

        Friendship saved = friendshipRepository.save(
            Friendship.request(command.requesterId(), command.recipientId()));
        return new Result(saved.getId());
    }
}
