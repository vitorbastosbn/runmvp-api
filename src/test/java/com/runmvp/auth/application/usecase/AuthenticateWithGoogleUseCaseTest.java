package com.runmvp.auth.application.usecase;

import com.runmvp.auth.application.port.in.AuthenticateWithGoogleUseCase;
import com.runmvp.auth.application.port.out.DeviceTokenRepository;
import com.runmvp.auth.application.port.out.GoogleTokenVerifier;
import com.runmvp.auth.application.port.out.JwtService;
import com.runmvp.auth.application.port.out.RefreshTokenRepository;
import com.runmvp.shared.config.JwtProperties;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.user.application.port.out.UserRepository;
import com.runmvp.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateWithGoogleUseCaseTest {

    @Mock private GoogleTokenVerifier googleVerifier;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private DeviceTokenRepository deviceTokenRepository;

    private AuthenticateWithGoogleUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties("secret", 900L, 30L);
        useCase = new AuthenticateWithGoogleUseCaseImpl(
            googleVerifier,
            userRepository,
            jwtService,
            refreshTokenRepository,
            deviceTokenRepository,
            props
        );
    }

    @Test
    void execute_newUser_createsUserAndReturnsTokens() {
        when(googleVerifier.verify("valid-token")).thenReturn(
            new GoogleTokenVerifier.GoogleIdTokenPayload("sub-new", "new@x.com", "New User", "https://av.io/img")
        );
        when(userRepository.findByGoogleSubject("sub-new")).thenReturn(Optional.empty());
        when(userRepository.existsByPublicCode(any())).thenReturn(false);
        User savedUser = User.create("sub-new", "New User", "new@x.com", "https://av.io/img", "CODE1234");
        savedUser.setId(1L);
        when(userRepository.save(any())).thenReturn(savedUser);
        when(jwtService.generateAccessToken(1L)).thenReturn("access-jwt");

        AuthenticateWithGoogleUseCase.Result result =
            useCase.execute(new AuthenticateWithGoogleUseCase.Command("valid-token", "fcm-abc"));

        assertThat(result.isNewUser()).isTrue();
        assertThat(result.accessToken()).isEqualTo("access-jwt");
        assertThat(result.refreshToken()).isNotBlank();
        verify(refreshTokenRepository).save(any());
        verify(deviceTokenRepository).upsert(1L, "fcm-abc");
    }

    @Test
    void execute_existingUser_returnsTokensAndIsNewUserFalse() {
        when(googleVerifier.verify("valid-token")).thenReturn(
            new GoogleTokenVerifier.GoogleIdTokenPayload("sub-exist", "e@x.com", "Exist User", null)
        );
        User existing = User.create("sub-exist", "Exist User", "e@x.com", null, "EXIST234");
        existing.setId(2L);
        when(userRepository.findByGoogleSubject("sub-exist")).thenReturn(Optional.of(existing));
        when(jwtService.generateAccessToken(2L)).thenReturn("access-exist");

        AuthenticateWithGoogleUseCase.Result result =
            useCase.execute(new AuthenticateWithGoogleUseCase.Command("valid-token", null));

        assertThat(result.isNewUser()).isFalse();
        assertThat(result.accessToken()).isEqualTo("access-exist");
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.user().getId()).isEqualTo(2L);
        verify(refreshTokenRepository).save(any());
        verifyNoInteractions(deviceTokenRepository);
    }

    @Test
    void execute_deletedUser_throwsAccountDeleted() {
        when(googleVerifier.verify("valid-token")).thenReturn(
            new GoogleTokenVerifier.GoogleIdTokenPayload("sub-del", "d@x.com", "Del", null)
        );
        User deleted = User.create("sub-del", "Del", "d@x.com", null, "DEL01234");
        deleted.setId(3L);
        deleted.softDelete();
        when(userRepository.findByGoogleSubject("sub-del")).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() ->
            useCase.execute(new AuthenticateWithGoogleUseCase.Command("valid-token", null)))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.ACCOUNT_DELETED);
    }

    @Test
    void execute_invalidGoogleToken_throwsGoogleTokenInvalid() {
        when(googleVerifier.verify("bad")).thenThrow(new BusinessException(ErrorCode.GOOGLE_TOKEN_INVALID));

        assertThatThrownBy(() ->
            useCase.execute(new AuthenticateWithGoogleUseCase.Command("bad", null)))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.GOOGLE_TOKEN_INVALID);
    }
}
