package com.pibank.backend.domain.entity;

import com.pibank.backend.domain.enums.AtmCodeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pb_atm_codes", indexes = {
    @Index(name = "idx_atm_user", columnList = "user_id"),
    @Index(name = "idx_atm_account", columnList = "account_id"),
    @Index(name = "idx_atm_status", columnList = "status"),
    @Index(name = "idx_atm_expires", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtmCode extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Código hasheado con BCrypt para seguridad
    @Column(name = "code_hash", length = 255, nullable = false)
    private String codeHash;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private AtmCodeStatus status = AtmCodeStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "used_at_ip", length = 45)
    private String usedAtIp;

    @Column(name = "atm_identifier", length = 100)
    private String atmIdentifier;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
