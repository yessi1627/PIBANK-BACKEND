package com.pibank.backend.domain.enums;

public enum CardStatus {
    ACTIVE,     // Tarjeta activa
    BLOCKED,    // Tarjeta bloqueada (por el usuario o por seguridad)
    EXPIRED,    // Tarjeta expirada
    CANCELLED   // Tarjeta cancelada permanentemente
}
