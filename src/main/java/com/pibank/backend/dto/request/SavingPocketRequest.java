package com.pibank.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SavingPocketRequest {

    @NotBlank(message = "La cuenta es requerida")
    private String accountId;

    @NotBlank(message = "El nombre del bolsillo es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @Size(max = 10, message = "El emoji no puede superar 10 caracteres")
    private String emoji;

    private BigDecimal targetAmount;

    private LocalDate targetDate;

    private Boolean roundingEnabled;

    private BigDecimal roundingMultiplier;
}
