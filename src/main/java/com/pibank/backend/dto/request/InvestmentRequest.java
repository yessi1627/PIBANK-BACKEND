package com.pibank.backend.dto.request;

import com.pibank.backend.domain.enums.InvestmentType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvestmentRequest {

    @NotBlank(message = "La cuenta de origen es requerida")
    private String accountId;

    @NotNull(message = "El tipo de inversión es requerido")
    private InvestmentType investmentType;

    @NotNull(message = "El monto de inversión es requerido")
    @DecimalMin(value = "5000.00", message = "El monto mínimo de inversión es $5.000")
    private BigDecimal amount;

    @Min(value = 30, message = "El plazo mínimo es 30 días")
    @Max(value = 1825, message = "El plazo máximo es 1825 días (5 años)")
    private Integer termDays;

    private Boolean autoReinvest;

    @Size(max = 100, message = "El alias no puede superar 100 caracteres")
    private String alias;
}
