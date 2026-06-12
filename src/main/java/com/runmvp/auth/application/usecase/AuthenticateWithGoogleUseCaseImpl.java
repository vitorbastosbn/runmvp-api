package com.runmvp.auth.application.usecase;

import com.runmvp.auth.application.port.in.AuthenticateWithGoogleUseCase;
import com.runmvp.auth.application.port.out.DeviceTokenRepository;
import com.runmvp.auth.application.port.out.GoogleTokenVerifier;
import com.runmvp.auth.application.port.out.JwtService;
import com.runmvp.auth.application.port.out.RefreshTokenRepository;
import com.runmvp.auth.domain.RefreshToken;
import com.runmvp.shared.config.JwtProperties;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import com.runmvp.shared.util.PublicCodeGenerator;
import com.runmvp.user.application.port.out.UserRepository;
import com.runmvp.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticateWithGoogleUseCaseImpl implements AuthenticateWithGoogleUseCase {

    private final GoogleTokenVerifier googleVerifier;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final JwtProperties jwtProperties;

    public AuthenticateWithGoogleUseCaseImpl(
            GoogleTokenVerifier googleVerifier,
            UserRepository userRepository,
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository,
            DeviceTokenRepository deviceTokenRepository,
            JwtProperties jwtProperties) {
        this.googleVerifier = googleVerifier;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional
    public Result execute(Command command) {
        GoogleTokenVerifier.GoogleIdTokenPayload googlePayload = googleVerifier.verify(command.idToken());

        return userRepository.findByGoogleSubject(googlePayload.subject())
            .map(user -> authenticateExistingUser(user, command.fcmToken()))
            .orElseGet(() -> registerNewUser(googlePayload, command.fcmToken()));
    }

    private Result authenticateExistingUser(User user, String fcmToken) {
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.ACCOUNT_DELETED);
        }
        return issueTokens(user, fcmToken, false);
    }

    private Result registerNewUser(GoogleTokenVerifier.GoogleIdTokenPayload payload, String fcmToken) {
        User user = User.create(
            payload.subject(),
            payload.name(),
            payload.email(),
            payload.pictureUrl(),
            generateUniquePublicCode()
        );
        User saved = userRepository.save(user);
        return issueTokens(saved, fcmToken, true);
    }

    private Result issueTokens(User user, String fcmToken, boolean isNewUser) {
        String accessToken = jwtService.generateAccessToken(user.getId());
        RefreshToken refreshToken = RefreshToken.issue(user.getId(), jwtProperties.refreshTokenExpirationDays());
        refreshTokenRepository.save(refreshToken);

        if (fcmToken != null && !fcmToken.isBlank()) {
            deviceTokenRepository.upsert(user.getId(), fcmToken);
        }

        return new Result(accessToken, refreshToken.getRawToken(), user, isNewUser);
    }

    private String generateUniquePublicCode() {
        for (int attempts = 0; attempts < 10; attempts++) {
            String code = PublicCodeGenerator.generate();
            if (!userRepository.existsByPublicCode(code)) {
                return code;
            }
        }
        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }
}
