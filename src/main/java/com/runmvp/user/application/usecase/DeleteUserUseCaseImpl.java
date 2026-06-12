package com.runmvp.user.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.shared.outbox.OutboxEvent;
import com.runmvp.shared.outbox.OutboxEventRepository;
import com.runmvp.user.application.port.in.DeleteUserUseCase;
import com.runmvp.user.application.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteUserUseCaseImpl implements DeleteUserUseCase {
    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public DeleteUserUseCaseImpl(UserRepository userRepository,
                                 OutboxEventRepository outboxEventRepository,
                                 ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void execute(Long userId) {
        var user = userRepository.findById(userId)
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.softDelete();
        userRepository.save(user);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("userId", userId);
        outboxEventRepository.publish(OutboxEvent.of("user", userId, "user.deleted", payload));
    }
}
