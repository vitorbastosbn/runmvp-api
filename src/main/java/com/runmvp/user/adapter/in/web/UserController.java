package com.runmvp.user.adapter.in.web;

import com.runmvp.shared.security.AuthenticatedUser;
import com.runmvp.user.application.port.in.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final GetCurrentUserUseCase getCurrentUser;
    private final UpdateUserUseCase updateUser;
    private final DeleteUserUseCase deleteUser;
    private final RegisterDeviceTokenUseCase registerDeviceToken;
    private final GetUserStatisticsUseCase getUserStatistics;
    private final FindUserByCodeUseCase findUserByCode;

    public UserController(GetCurrentUserUseCase getCurrentUser,
                          UpdateUserUseCase updateUser,
                          DeleteUserUseCase deleteUser,
                          RegisterDeviceTokenUseCase registerDeviceToken,
                          GetUserStatisticsUseCase getUserStatistics,
                          FindUserByCodeUseCase findUserByCode) {
        this.getCurrentUser = getCurrentUser;
        this.updateUser = updateUser;
        this.deleteUser = deleteUser;
        this.registerDeviceToken = registerDeviceToken;
        this.getUserStatistics = getUserStatistics;
        this.findUserByCode = findUserByCode;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        var result = getCurrentUser.execute(principal.userId());
        return ResponseEntity.ok(UserResponse.from(result.user(),
            result.entitlement().name()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> patchMe(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody PatchMeRequest req) {
        updateUser.execute(new UpdateUserUseCase.Command(
            principal.userId(), req.name(), req.avatarUrl()));
        var result = getCurrentUser.execute(principal.userId());
        return ResponseEntity.ok(UserResponse.from(result.user(),
            result.entitlement().name()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        deleteUser.execute(principal.userId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/device-token")
    public ResponseEntity<Void> putDeviceToken(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody DeviceTokenRequest req) {
        registerDeviceToken.execute(
            new RegisterDeviceTokenUseCase.Command(principal.userId(), req.fcmToken()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{id}/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id) {
        var stats = getUserStatistics.execute(id, principal.userId());
        return ResponseEntity.ok(StatisticsResponse.from(stats));
    }

    @GetMapping("/users/by-code/{publicCode}")
    public ResponseEntity<UserSummaryResponse> getByCode(@PathVariable String publicCode) {
        return ResponseEntity.ok(UserSummaryResponse.from(findUserByCode.execute(publicCode)));
    }

    public record PatchMeRequest(String name, String avatarUrl) {}
    public record DeviceTokenRequest(@NotBlank String fcmToken) {}
}
