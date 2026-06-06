package com.pibank.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavingPocketResponse {

    private String id;
    private String name;
    private String emoji;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal progressPercentage;
    private LocalDate targetDate;
    private Boolean roundingEnabled;
    private BigDecimal roundingMultiplier;
    private Boolean isActive;
    private Boolean isCompleted;
    private LocalDateTime createdAt;
}
