package com.pibank.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRequest {

    @NotBlank(message = "La cuenta de desembolso es requerida")
    private String disbursementAccountId;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "500000.00", message = "El monto mínimo del crédito es $500.000")
    @DecimalMax(value = "200000000.00", message = "El monto máximo del crédito es $200.000.000")
    private BigDecimal requestedAmount;

    @NotNull(message = "El plazo en meses es requerido")
    @Min(value = 6, message = "El plazo mínimo es 6 meses")
    @Max(value = 120, message = "El plazo máximo es 120 meses")
    private Integer termMonths;

    @NotBlank(message = "El propósito del crédito es requerido")
    @Size(min = 10, max = 255, message = "El propósito debe tener entre 10 y 255 caracteres")
    private String purpose;

    @Pattern(regexp = "^[0-9]{6}$", message = "El código MFA debe tener 6 dígitos")
    private String mfaCode;
}
