package com.pibank.backend.domain.enums;

public enum AlertStatus {
    OPEN,               // Alerta abierta, sin investigar
    INVESTIGATING,      // En investigación por un analista
    RESOLVED,           // Resuelta — fraude confirmado
    FALSE_POSITIVE      // Resuelta — no fue fraude
}
