package com.pibank.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pibank.backend.domain.enums.CardStatus;
import com.pibank.backend.domain.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VirtualCardResponse {

    private String id;
    private String lastFourDigits;
    private CardType cardType;
    private CardStatus status;
    private String expiryYearMonth;
    private String cvv;
    private LocalDateTime cvvExpiresAt;
    private String alias;
    private String cardHolderName;
    private String accountNumber;
    private LocalDateTime createdAt;
}
