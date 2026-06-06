package com.pibank.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse {

    private UserResponse user;
    private List<AccountResponse> accounts;
    private BigDecimal totalBalance;
    private BigDecimal totalAvailableBalance;
    private List<TransactionResponse> recentTransactions;
    private List<InvestmentResponse> investments;
    private List<SavingPocketResponse> savingPockets;
    private BigDecimal totalInvested;
    private BigDecimal totalSaved;
    private Map<String, BigDecimal> spendingByCategory;
    private Integer pendingAlerts;
    private LoanResponse activeLoan;
}
