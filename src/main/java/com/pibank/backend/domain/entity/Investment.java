package com.pibank.backend.domain.entity;

import com.pibank.backend.domain.enums.InvestmentStatus;
import com.pibank.backend.domain.enums.InvestmentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pb_investments", indexes = {
    @Index(name = "idx_investments_user", columnList = "user_id"),
    @Index(name = "idx_investments_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "investment_number", length = 20, nullable = false, unique = true)
    private String investmentNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_type", length = 30, nullable = false)
    private InvestmentType investmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private InvestmentStatus status = InvestmentStatus.ACTIVE;

    @Column(name = "initial_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal initialAmount;

    @Column(name = "current_value", precision = 18, scale = 2, nullable = false)
    private BigDecimal currentValue;

    @Column(name = "earned_returns", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal earnedReturns = BigDecimal.ZERO;

    @Column(name = "annual_interest_rate", precision = 6, scale = 4, nullable = false)
    private BigDecimal annualInterestRate;

    @Column(name = "term_days")
    private Integer termDays;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "auto_reinvest", nullable = false)
    @Builder.Default
    private Boolean autoReinvest = false;

    @Column(name = "alias", length = 100)
    private String alias;
}
