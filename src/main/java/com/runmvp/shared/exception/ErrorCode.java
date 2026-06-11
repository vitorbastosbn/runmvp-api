package com.runmvp.shared.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    GOOGLE_TOKEN_INVALID(HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    PREMIUM_REQUIRED(HttpStatus.FORBIDDEN);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) { this.httpStatus = httpStatus; }

    public HttpStatus getHttpStatus() { return httpStatus; }
}
