package com.pibank.backend.domain.enums;

public enum AlertType {
    UNUSUAL_LOCATION,       // Acceso desde ubicación inusual
    LARGE_AMOUNT,           // Transacción por monto inusualmente alto
    MULTIPLE_ATTEMPTS,      // Múltiples intentos fallidos de login
    UNUSUAL_TIME,           // Transacción a hora inusual
    RAPID_TRANSACTIONS,     // Múltiples transacciones en corto tiempo
    FOREIGN_TRANSACTION,    // Transacción desde país extranjero
    ACCOUNT_TAKEOVER,       // Posible robo de cuenta
    CARD_FRAUD,             // Posible fraude con tarjeta
    VELOCITY_CHECK          // Velocidad anormal de transacciones
}
