package com.pibank.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pibank.backend.domain.enums.TransactionCategory;
import com.pibank.backend.domain.enums.TransactionStatus;
import com.pibank.backend.domain.enums.TransactionType;
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
public class TransactionResponse {

    private String id;
    private String transactionNumber;
    private TransactionType type;
    private TransactionStatus status;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String description;
    private TransactionCategory category;
    private String reference;
    private BigDecimal fraudScore;
    private String failureReason;
    private BigDecimal balanceAfter;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
