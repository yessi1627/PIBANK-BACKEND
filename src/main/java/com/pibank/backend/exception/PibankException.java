package com.pibank.backend.exception;

import org.springframework.http.HttpStatus;

public class PibankException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public PibankException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public PibankException(String message, HttpStatus status) {
        this(message, status, "PIBANK_ERROR");
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
