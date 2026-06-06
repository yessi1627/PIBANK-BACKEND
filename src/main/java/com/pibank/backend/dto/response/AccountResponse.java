package com.pibank.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pibank.backend.domain.enums.AccountStatus;
import com.pibank.backend.domain.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {

    private String id;
    private String accountNumber;
    private AccountType accountType;
    private AccountStatus status;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private String currency;
    private String alias;
    private BigDecimal creditLimit;
    private BigDecimal interestRate;
    private Integer cutoffDay;
    private Integer paymentDay;
    private Boolean roundingEnabled;
    private LocalDateTime lastMovementAt;
    private LocalDateTime createdAt;
}
