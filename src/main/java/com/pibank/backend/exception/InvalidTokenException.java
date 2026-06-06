package com.pibank.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends PibankException {

    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }

    public InvalidTokenException() {
        super("Token inválido o expirado", HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }
}
