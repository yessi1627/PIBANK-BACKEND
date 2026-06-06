package com.pibank.backend.domain.enums;

public enum TransactionStatus {
    PENDING,        // Pendiente de procesamiento
    COMPLETED,      // Completada exitosamente
    FAILED,         // Falló durante el procesamiento
    REVERSED,       // Revertida
    FRAUD_BLOCKED   // Bloqueada por sistema de detección de fraude
}
