package com.pibank.backend.exception;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class InsufficientFundsException extends PibankException {

    public InsufficientFundsException(BigDecimal available, BigDecimal required) {
        super(
            String.format("Saldo insuficiente. Disponible: $%s, Requerido: $%s", available, required),
            HttpStatus.UNPROCESSABLE_ENTITY,
            "INSUFFICIENT_FUNDS"
        );
    }

    public InsufficientFundsException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_FUNDS");
    }
}
