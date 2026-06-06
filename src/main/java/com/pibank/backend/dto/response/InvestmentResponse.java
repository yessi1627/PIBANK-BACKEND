package com.pibank.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pibank.backend.domain.enums.InvestmentStatus;
import com.pibank.backend.domain.enums.InvestmentType;
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
public class InvestmentResponse {

    private String id;
    private String investmentNumber;
    private InvestmentType investmentType;
    private InvestmentStatus status;
    private BigDecimal initialAmount;
    private BigDecimal currentValue;
    private BigDecimal earnedReturns;
    private BigDecimal returnPercentage;
    private BigDecimal annualInterestRate;
    private Integer termDays;
    private LocalDate maturityDate;
    private Boolean autoReinvest;
    private String alias;
    private LocalDateTime createdAt;
}
