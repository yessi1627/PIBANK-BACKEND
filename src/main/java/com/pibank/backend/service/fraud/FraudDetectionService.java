package com.pibank.backend.service.fraud;

import com.pibank.backend.domain.entity.Account;
import com.pibank.backend.domain.entity.FraudAlert;
import com.pibank.backend.domain.entity.Transaction;
import com.pibank.backend.domain.entity.User;
import com.pibank.backend.domain.enums.AlertSeverity;
import com.pibank.backend.domain.enums.AlertStatus;
import com.pibank.backend.domain.enums.AlertType;
import com.pibank.backend.exception.FraudDetectedException;
import com.pibank.backend.repository.FraudAlertRepository;
import com.pibank.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final FraudAlertRepository fraudAlertRepository;
    private final TransactionRepository transactionRepository;

    @Value("${pibank.fraud.suspicious-amount-threshold}")
    private BigDecimal suspiciousAmountThreshold;

    @Value("${pibank.fraud.suspicious-location-enabled}")
    private boolean locationCheckEnabled;

    private static final int MAX_TRANSACTIONS_PER_HOUR = 10;
    private static final BigDecimal HIGH_RISK_SCORE = new BigDecimal("0.80");
    private static final BigDecimal BLOCK_SCORE = new BigDecimal("0.95");

    @Transactional
    public BigDecimal evaluateTransaction(
            User user, Account fromAccount, BigDecimal amount,
            String ipAddress, String deviceInfo, String locationInfo) {

        BigDecimal score = BigDecimal.ZERO;
        List<String> reasons = new ArrayList<>();

        // Regla 1: Monto sospechosamente alto
        if (amount.compareTo(suspiciousAmountThreshold) > 0) {
            score = score.add(new BigDecimal("0.40"));
            reasons.add("Monto superior a " + suspiciousAmountThreshold);
        }

        // Regla 2: Velocidad de transacciones (más de 10 en 1 hora)
        long recentCount = transactionRepository.countRecentTransactionsByUser(
            user.getId(), LocalDateTime.now().minusHours(1)
        );
        if (recentCount > MAX_TRANSACTIONS_PER_HOUR) {
            score = score.add(new BigDecimal("0.35"));
            reasons.add("Alta velocidad: " + recentCount + " transacciones en la última hora");
        }

        // Regla 3: Hora inusual (madrugada: 1am - 5am)
        int hour = LocalDateTime.now().getHour();
        if (hour >= 1 && hour <= 5) {
            score = score.add(new BigDecimal("0.15"));
            reasons.add("Transacción en horario inusual: " + hour + ":00");
        }

        // Regla 4: Ubicación diferente (si está habilitado y hay datos)
        if (locationCheckEnabled && locationInfo != null && user.getLastLoginIp() != null
                && !ipAddress.equals(user.getLastLoginIp())) {
            score = score.add(new BigDecimal("0.20"));
            reasons.add("IP diferente a la habitual");
        }

        // Limitar score a máximo 1.0
        if (score.compareTo(BigDecimal.ONE) > 0) {
            score = BigDecimal.ONE;
        }

        // Crear alerta si el score es alto
        if (score.compareTo(HIGH_RISK_SCORE) >= 0) {
            AlertSeverity severity = score.compareTo(BLOCK_SCORE) >= 0
                ? AlertSeverity.CRITICAL : AlertSeverity.HIGH;

            createAlert(user, null, AlertType.LARGE_AMOUNT, severity,
                String.join("; ", reasons), ipAddress, deviceInfo, locationInfo);

            if (score.compareTo(BLOCK_SCORE) >= 0) {
                log.warn("Transacción BLOQUEADA por fraude. Usuario: {}, Score: {}", user.getEmail(), score);
                throw new FraudDetectedException(String.join("; ", reasons));
            }

            log.warn("Alerta de fraude generada. Usuario: {}, Score: {}", user.getEmail(), score);
        }

        return score;
    }

    @Transactional
    public void createAlert(User user, Transaction transaction, AlertType type,
                             AlertSeverity severity, String description,
                             String ip, String device, String location) {
        FraudAlert alert = FraudAlert.builder()
            .user(user)
            .transaction(transaction)
            .alertType(type)
            .severity(severity)
            .status(AlertStatus.OPEN)
            .description(description)
            .ipAddress(ip)
            .deviceInfo(device)
            .locationInfo(location)
            .build();
        fraudAlertRepository.save(alert);
    }
}
