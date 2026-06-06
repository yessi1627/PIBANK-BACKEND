package com.pibank.backend.domain.enums;

public enum AlertSeverity {
    LOW,        // Riesgo bajo — monitoreo
    MEDIUM,     // Riesgo medio — notificar al usuario
    HIGH,       // Riesgo alto — bloquear transacción, notificar analista
    CRITICAL    // Riesgo crítico — bloquear cuenta inmediatamente
}
