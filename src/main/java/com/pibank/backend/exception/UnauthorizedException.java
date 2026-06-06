package com.pibank.backend.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends PibankException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public UnauthorizedException() {
        super("No autorizado para realizar esta operación", HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
