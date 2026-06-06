package com.pibank.backend.exception;

import org.springframework.http.HttpStatus;

public class MfaRequiredException extends PibankException {

    public MfaRequiredException() {
        super(
            "Se requiere verificación de doble factor (MFA) para continuar",
            HttpStatus.FORBIDDEN,
            "MFA_REQUIRED"
        );
    }
}
