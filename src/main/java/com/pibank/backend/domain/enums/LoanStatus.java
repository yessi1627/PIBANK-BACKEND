package com.pibank.backend.domain.enums;

public enum LoanStatus {
    PENDING_REVIEW,     // En revisión por el banco
    APPROVED,           // Aprobado, pendiente de desembolso
    ACTIVE,             // Activo, en pago
    PAID,               // Pagado completamente
    DEFAULTED,          // En mora
    REJECTED,           // Rechazado
    CANCELLED           // Cancelado antes del desembolso
}
