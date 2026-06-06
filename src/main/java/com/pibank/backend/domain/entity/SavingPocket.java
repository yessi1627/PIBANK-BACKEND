package com.pibank.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pb_saving_pockets", indexes = {
    @Index(name = "idx_pockets_user", columnList = "user_id"),
    @Index(name = "idx_pockets_account", columnList = "account_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingPocket extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "emoji", length = 10)
    private String emoji;

    @Column(name = "target_amount", precision = 18, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "rounding_enabled", nullable = false)
    @Builder.Default
    private Boolean roundingEnabled = false;

    @Column(name = "rounding_multiplier", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal roundingMultiplier = BigDecimal.ONE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    public BigDecimal getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount.multiply(new BigDecimal("100")).divide(targetAmount, 2, java.math.RoundingMode.HALF_UP);
    }
}
