package com.pibank.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pibank.backend.domain.enums.LoanStatus;
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
public class LoanResponse {

    private String id;
    private String loanNumber;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal outstandingBalance;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal monthlyPayment;
    private String purpose;
    private Integer creditScore;
    private LoanStatus status;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private LocalDateTime disbursedAt;
    private LocalDate firstPaymentDate;
    private LocalDate nextPaymentDate;
    private Integer paymentsMade;
    private Integer daysOverdue;
    private LocalDateTime createdAt;
}
