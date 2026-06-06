package com.pibank.backend.domain.entity;

import com.pibank.backend.domain.enums.TransactionCategory;
import com.pibank.backend.domain.enums.TransactionStatus;
import com.pibank.backend.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pb_transactions", indexes = {
    @Index(name = "idx_transactions_from_account", columnList = "from_account_id"),
    @Index(name = "idx_transactions_to_account", columnList = "to_account_id"),
    @Index(name = "idx_transactions_reference", columnList = "reference"),
    @Index(name = "idx_transactions_created_at", columnList = "created_at"),
    @Index(name = "idx_transactions_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {

    @Column(name = "transaction_number", length = 30, nullable = false, unique = true)
    private String transactionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "COP";

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 40)
    private TransactionCategory category;

    @Column(name = "reference", length = 50)
    private String reference;

    // Datos de contexto de seguridad (JSON)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "location_info", length = 255)
    private String locationInfo;

    @Column(name = "fraud_score", precision = 5, scale = 4)
    private BigDecimal fraudScore;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // Para reversiones — apunta a la transacción original
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_transaction_id")
    private Transaction originalTransaction;

    @Column(name = "balance_after_from", precision = 18, scale = 2)
    private BigDecimal balanceAfterFrom;

    @Column(name = "balance_after_to", precision = 18, scale = 2)
    private BigDecimal balanceAfterTo;
}
