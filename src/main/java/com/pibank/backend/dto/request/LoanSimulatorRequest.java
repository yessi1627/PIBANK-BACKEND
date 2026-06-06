package com.pibank.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanSimulatorRequest {

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "500000.00", message = "El monto mínimo del crédito es $500.000")
    @DecimalMax(value = "200000000.00", message = "El monto máximo del crédito es $200.000.000")
    private BigDecimal amount;

    @NotNull(message = "El plazo en meses es requerido")
    @Min(value = 6, message = "El plazo mínimo es 6 meses")
    @Max(value = 120, message = "El plazo máximo es 120 meses (10 años)")
    private Integer termMonths;
}
