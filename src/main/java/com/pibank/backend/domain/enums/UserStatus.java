package com.pibank.backend.domain.enums;

public enum UserStatus {
    ACTIVE,                 // Usuario activo
    INACTIVE,               // Usuario inactivo (por voluntad propia)
    BLOCKED,                // Bloqueado por seguridad (intentos fallidos, fraude)
    PENDING_VERIFICATION,   // Registro pendiente de verificación de email
    SUSPENDED               // Suspendido por el administrador
}
