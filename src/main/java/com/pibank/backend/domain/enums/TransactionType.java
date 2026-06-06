package com.pibank.backend.domain.enums;

public enum TransactionType {
    TRANSFER,           // Transferencia entre cuentas
    PAYMENT,            // Pago de servicio/factura
    DEPOSIT,            // Depósito de dinero
    WITHDRAWAL,         // Retiro en ventanilla
    ATM_WITHDRAWAL,     // Retiro en cajero automático (con código)
    PURCHASE,           // Compra con tarjeta
    VIRTUAL_PURCHASE,   // Compra con tarjeta virtual
    REVERSAL,           // Reversión de transacción
    LOAN_DISBURSEMENT,  // Desembolso de crédito
    LOAN_PAYMENT,       // Pago de cuota de crédito
    INVESTMENT,         // Inversión en fondo/CDT
    INVESTMENT_RETURN,  // Retorno de inversión
    ROUNDING,           // Redondeo automático al bolsillo de ahorro
    RECHARGE            // Recarga de celular/transporte
}
