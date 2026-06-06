package com.pibank.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pibank.backend.domain.enums.AtmCodeStatus;
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
public class AtmCodeResponse {

    private String id;
    private String code;
    private BigDecimal amount;
    private AtmCodeStatus status;
    private LocalDateTime expiresAt;
    private String accountNumber;
    private LocalDateTime createdAt;
    private Long remainingSeconds;
}
