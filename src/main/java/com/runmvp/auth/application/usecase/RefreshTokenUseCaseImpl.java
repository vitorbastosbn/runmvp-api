package com.runmvp.auth.application.usecase;

import com.runmvp.auth.application.port.in.RefreshTokenUseCase;
import com.runmvp.auth.application.port.out.JwtService;
import com.runmvp.auth.application.port.out.RefreshTokenRepository;
import com.runmvp.auth.domain.RefreshToken;
import com.runmvp.shared.config.JwtProperties;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public RefreshTokenUseCaseImpl(
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional
    public Result execute(Command command) {
        String tokenHash = RefreshToken.hash(command.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (stored.isRevoked()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        if (stored.isExpired()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        refreshTokenRepository.revokeByTokenHash(stored.getTokenHash());

        RefreshToken newToken = RefreshToken.issue(stored.getUserId(), jwtProperties.refreshTokenExpirationDays());
        refreshTokenRepository.save(newToken);

        return new Result(jwtService.generateAccessToken(stored.getUserId()), newToken.getRawToken());
    }
}
