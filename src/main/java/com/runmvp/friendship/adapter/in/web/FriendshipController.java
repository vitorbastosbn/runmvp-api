package com.runmvp.friendship.adapter.in.web;

import com.runmvp.friendship.application.port.in.*;
import com.runmvp.shared.security.AuthenticatedUser;
import com.runmvp.user.adapter.in.web.UserSummaryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class FriendshipController {

    private final ListFriendsUseCase listFriends;
    private final ListFriendRequestsUseCase listRequests;
    private final SendFriendRequestUseCase sendRequest;
    private final AcceptFriendRequestUseCase acceptRequest;
    private final RejectFriendRequestUseCase rejectRequest;
    private final RemoveFriendUseCase removeFriend;

    public FriendshipController(ListFriendsUseCase listFriends,
                                ListFriendRequestsUseCase listRequests,
                                SendFriendRequestUseCase sendRequest,
                                AcceptFriendRequestUseCase acceptRequest,
                                RejectFriendRequestUseCase rejectRequest,
                                RemoveFriendUseCase removeFriend) {
        this.listFriends = listFriends; this.listRequests = listRequests;
        this.sendRequest = sendRequest; this.acceptRequest = acceptRequest;
        this.rejectRequest = rejectRequest; this.removeFriend = removeFriend;
    }

    @GetMapping("/friends")
    public Page<UserSummaryResponse> listFriends(
            @AuthenticationPrincipal AuthenticatedUser p, Pageable pageable) {
        return listFriends.execute(p.userId(), pageable)
            .map(UserSummaryResponse::from);
    }

    @GetMapping("/friend-requests")
    public Page<FriendRequestResponse> listRequests(
            @AuthenticationPrincipal AuthenticatedUser p, Pageable pageable) {
        return listRequests.execute(p.userId(), pageable)
            .map(e -> new FriendRequestResponse(e.id(), UserSummaryResponse.from(e.requester()),
                e.createdAt().toString()));
    }

    @PostMapping("/friend-requests")
    public ResponseEntity<SendFriendRequestUseCase.Result> sendRequest(
            @AuthenticationPrincipal AuthenticatedUser p,
            @Valid @RequestBody SendRequestBody body) {
        return ResponseEntity.status(201).body(
            sendRequest.execute(new SendFriendRequestUseCase.Command(p.userId(), body.recipientId())));
    }

    @PostMapping("/friend-requests/{id}/accept")
    public ResponseEntity<Void> accept(@AuthenticationPrincipal AuthenticatedUser p,
                                       @PathVariable Long id) {
        acceptRequest.execute(id, p.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/friend-requests/{id}/reject")
    public ResponseEntity<Void> reject(@AuthenticationPrincipal AuthenticatedUser p,
                                       @PathVariable Long id) {
        rejectRequest.execute(id, p.userId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@AuthenticationPrincipal AuthenticatedUser p,
                                             @PathVariable Long friendId) {
        removeFriend.execute(p.userId(), friendId);
        return ResponseEntity.noContent().build();
    }

    record SendRequestBody(@NotNull Long recipientId) {}
    record FriendRequestResponse(Long id, UserSummaryResponse requester, String createdAt) {}
}
