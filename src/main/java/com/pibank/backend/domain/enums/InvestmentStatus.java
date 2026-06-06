package com.pibank.backend.domain.enums;

public enum InvestmentStatus {
    ACTIVE,     // Inversión activa
    MATURED,    // Inversión vencida (CDT cumplió su plazo)
    CANCELLED   // Cancelada antes del vencimiento (con penalidad)
}
