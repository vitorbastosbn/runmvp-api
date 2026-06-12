package com.runmvp.shared.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handle_businessException_usesErrorCodeStatusAndName() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handle(new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("REFRESH_TOKEN_INVALID");
        assertThat(response.getBody().message()).isEqualTo("REFRESH_TOKEN_INVALID");
        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    void handleGeneric_returnsInternalErrorWithoutExceptionDetails() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleGeneric(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).isEqualTo("Internal server error");
        assertThat(response.getBody().details()).isEmpty();
    }
}
