package com.pibank.backend.exception;

import org.springframework.http.HttpStatus;

public class FraudDetectedException extends PibankException {

    public FraudDetectedException(String reason) {
        super(
            "Transacción bloqueada por detección de actividad sospechosa: " + reason,
            HttpStatus.FORBIDDEN,
            "FRAUD_DETECTED"
        );
    }
}
