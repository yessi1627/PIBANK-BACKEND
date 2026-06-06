package com.pibank.backend.exception;

import org.springframework.http.HttpStatus;

public class AccountBlockedException extends PibankException {

    public AccountBlockedException(String accountNumber) {
        super(
            "La cuenta " + accountNumber + " está bloqueada. Contacte a soporte.",
            HttpStatus.FORBIDDEN,
            "ACCOUNT_BLOCKED"
        );
    }
}
