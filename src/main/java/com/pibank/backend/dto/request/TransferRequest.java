package com.pibank.backend.dto.request;

import com.pibank.backend.domain.enums.TransactionCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotBlank(message = "La cuenta de origen es requerida")
    private String fromAccountId;

    @NotBlank(message = "La cuenta destino o número de celular es requerido")
    private String toAccountNumberOrPhone;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "1000.00", message = "El monto mínimo de transferencia es $1.000")
    @DecimalMax(value = "50000000.00", message = "El monto máximo por transferencia es $50.000.000")
    private BigDecimal amount;

    @Size(max = 200, message = "La descripción no puede superar 200 caracteres")
    private String description;

    private TransactionCategory category;

    @Pattern(regexp = "^[0-9]{6}$", message = "El código MFA debe tener 6 dígitos")
    private String mfaCode;
}
