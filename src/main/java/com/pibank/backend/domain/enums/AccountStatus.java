package com.pibank.backend.domain.enums;

public enum AccountStatus {
    ACTIVE,     // Cuenta activa y operativa
    INACTIVE,   // Cuenta inactiva
    FROZEN,     // Cuenta congelada (por investigación de fraude)
    CLOSED      // Cuenta cerrada permanentemente
}
