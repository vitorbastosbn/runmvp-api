package com.runmvp.auth.adapter.in.web;

import com.runmvp.auth.application.port.in.AuthenticateWithGoogleUseCase;
import com.runmvp.auth.application.port.in.LogoutUseCase;
import com.runmvp.auth.application.port.in.RefreshTokenUseCase;
import com.runmvp.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticateWithGoogleUseCase authenticateUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(
            AuthenticateWithGoogleUseCase authenticateUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase) {
        this.authenticateUseCase = authenticateUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody AuthRequest.Google request) {
        AuthenticateWithGoogleUseCase.Result result = authenticateUseCase.execute(
            new AuthenticateWithGoogleUseCase.Command(request.idToken(), request.fcmToken())
        );
        return ResponseEntity.ok(toResponse(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody AuthRequest.Refresh request) {
        RefreshTokenUseCase.Result result = refreshTokenUseCase.execute(
            new RefreshTokenUseCase.Command(request.refreshToken())
        );
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), result.newRefreshToken(), null, false));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody AuthRequest.Logout request) {
        logoutUseCase.execute(new LogoutUseCase.Command(request.refreshToken(), request.fcmToken()));
        return ResponseEntity.noContent().build();
    }

    private AuthResponse toResponse(AuthenticateWithGoogleUseCase.Result result) {
        User user = result.user();
        return new AuthResponse(
            result.accessToken(),
            result.refreshToken(),
            new AuthResponse.UserPayload(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getPublicCode(),
                "FREE"
            ),
            result.isNewUser()
        );
    }
}
