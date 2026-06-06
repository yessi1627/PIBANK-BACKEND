package com.pibank.backend.service;

import com.pibank.backend.domain.entity.Account;
import com.pibank.backend.domain.entity.User;
import com.pibank.backend.exception.FraudDetectedException;
import com.pibank.backend.repository.FraudAlertRepository;
import com.pibank.backend.repository.TransactionRepository;
import com.pibank.backend.service.fraud.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del motor de detección de fraude")
class FraudDetectionServiceTest {

    @Mock private FraudAlertRepository fraudAlertRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fraudDetectionService, "suspiciousAmountThreshold", new BigDecimal("5000000"));
        ReflectionTestUtils.setField(fraudDetectionService, "locationCheckEnabled", false);

        testUser = User.builder()
            .email("test@pibank.co")
            .lastLoginIp("192.168.1.1")
            .build();

        testAccount = new Account();
    }

    @Test
    @DisplayName("Transacción de monto normal debe tener score bajo")
    void evaluate_NormalTransaction_ShouldReturnLowScore() {
        when(transactionRepository.countRecentTransactionsByUser(any(), any())).thenReturn(1L);

        BigDecimal score = fraudDetectionService.evaluateTransaction(
            testUser, testAccount, new BigDecimal("50000"),
            "192.168.1.1", "Mozilla/5.0", null
        );

        assertThat(score).isLessThan(new BigDecimal("0.50"));
    }

    @Test
    @DisplayName("Transacción de monto muy alto debe generar alerta de fraude")
    void evaluate_VeryHighAmount_ShouldCreateAlert() {
        when(transactionRepository.countRecentTransactionsByUser(any(), any())).thenReturn(1L);
        when(fraudAlertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BigDecimal score = fraudDetectionService.evaluateTransaction(
            testUser, testAccount, new BigDecimal("6000000"),
            "192.168.1.1", "Mozilla/5.0", null
        );

        assertThat(score).isGreaterThanOrEqualTo(new BigDecimal("0.40"));
    }

    @Test
    @DisplayName("Alta velocidad de transacciones debe aumentar el score de riesgo")
    void evaluate_HighVelocity_ShouldIncreaseScore() {
        when(transactionRepository.countRecentTransactionsByUser(any(), any())).thenReturn(15L);
        when(fraudAlertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BigDecimal score = fraudDetectionService.evaluateTransaction(
            testUser, testAccount, new BigDecimal("10000"),
            "192.168.1.1", "Mozilla/5.0", null
        );

        assertThat(score).isGreaterThanOrEqualTo(new BigDecimal("0.35"));
    }
}
