package com.pibank.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanSimulatorResponse {

    private BigDecimal requestedAmount;
    private BigDecimal annualInterestRate;
    private BigDecimal monthlyInterestRate;
    private Integer termMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal totalPayment;
    private BigDecimal totalInterest;
    private List<PaymentScheduleItem> paymentSchedule;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentScheduleItem {
        private Integer installmentNumber;
        private BigDecimal payment;
        private BigDecimal principal;
        private BigDecimal interest;
        private BigDecimal remainingBalance;
        private String dueDate;
    }
}
