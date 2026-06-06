package com.pibank.backend.domain.enums;

public enum AtmCodeStatus {
    PENDING,    // Código generado, disponible para usar
    USED,       // Código ya utilizado en cajero
    EXPIRED,    // Código expirado (pasaron los 15 minutos)
    CANCELLED   // Cancelado por el usuario
}
