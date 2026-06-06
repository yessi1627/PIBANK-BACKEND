package com.pibank.backend.domain.entity;

import com.pibank.backend.domain.enums.CardStatus;
import com.pibank.backend.domain.enums.CardType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "pb_virtual_cards", indexes = {
    @Index(name = "idx_cards_account", columnList = "account_id"),
    @Index(name = "idx_cards_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualCard extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Número de tarjeta cifrado con AES-256
    @Column(name = "card_number_encrypted", length = 500, nullable = false)
    private String cardNumberEncrypted;

    // Últimos 4 dígitos visibles
    @Column(name = "last_four_digits", length = 4, nullable = false)
    private String lastFourDigits;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", length = 20, nullable = false)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private CardStatus status = CardStatus.ACTIVE;

    @Column(name = "expiry_year_month", length = 7, nullable = false)
    private String expiryYearMonth;

    // CVV dinámico cifrado — rota cada 5 minutos
    @Column(name = "cvv_encrypted", length = 255, nullable = false)
    private String cvvEncrypted;

    @Column(name = "cvv_last_rotated_at", nullable = false)
    private LocalDateTime cvvLastRotatedAt;

    @Column(name = "alias", length = 50)
    private String alias;

    @Column(name = "card_holder_name", length = 100, nullable = false)
    private String cardHolderName;

    @Column(name = "blocked_reason", length = 255)
    private String blockedReason;
}
