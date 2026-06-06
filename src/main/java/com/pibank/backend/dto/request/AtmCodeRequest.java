package com.pibank.backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AtmCodeRequest {

    @NotBlank(message = "La cuenta es requerida")
    private String accountId;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "20000.00", message = "El monto mínimo para retiro es $20.000")
    @DecimalMax(value = "2000000.00", message = "El monto máximo por código ATM es $2.000.000")
    private BigDecimal amount;
}
