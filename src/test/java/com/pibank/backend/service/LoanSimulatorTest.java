package com.pibank.backend.service;

import com.pibank.backend.dto.request.LoanSimulatorRequest;
import com.pibank.backend.dto.response.LoanSimulatorResponse;
import com.pibank.backend.repository.LoanRepository;
import com.pibank.backend.repository.UserRepository;
import com.pibank.backend.service.account.AccountService;
import com.pibank.backend.service.loan.LoanService;
import com.pibank.backend.util.AccountNumberGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del simulador de crédito")
class LoanSimulatorTest {

    @Mock private LoanRepository loanRepository;
    @Mock private UserRepository userRepository;
    @Mock private AccountService accountService;
    @Mock private AccountNumberGenerator numberGenerator;

    @InjectMocks
    private LoanService loanService;

    @Test
    @DisplayName("Debe calcular la cuota mensual correctamente para $1.000.000 a 12 meses")
    void simulateLoan_ShouldCalculateCorrectMonthlyPayment() {
        LoanSimulatorRequest request = new LoanSimulatorRequest();
        request.setAmount(new BigDecimal("1000000"));
        request.setTermMonths(12);

        LoanSimulatorResponse result = loanService.simulate(request);

        assertThat(result).isNotNull();
        assertThat(result.getMonthlyPayment()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getTotalPayment()).isGreaterThan(result.getRequestedAmount());
        assertThat(result.getTotalInterest()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getPaymentSchedule()).hasSize(12);
    }

    @Test
    @DisplayName("La suma de cuotas de capital debe ser igual al monto solicitado")
    void simulateLoan_SumOfPrincipalsShouldEqualRequestedAmount() {
        LoanSimulatorRequest request = new LoanSimulatorRequest();
        request.setAmount(new BigDecimal("5000000"));
        request.setTermMonths(24);

        LoanSimulatorResponse result = loanService.simulate(request);

        BigDecimal totalPrincipal = result.getPaymentSchedule().stream()
            .map(LoanSimulatorResponse.PaymentScheduleItem::getPrincipal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // La diferencia debe ser menor a $1 por redondeo
        assertThat(totalPrincipal.subtract(request.getAmount()).abs())
            .isLessThanOrEqualTo(new BigDecimal("1.00"));
    }

    @Test
    @DisplayName("El plan de pagos debe tener exactamente termMonths cuotas")
    void simulateLoan_PaymentScheduleSizeShouldMatchTermMonths() {
        for (int months : new int[]{6, 12, 24, 36, 60}) {
            LoanSimulatorRequest request = new LoanSimulatorRequest();
            request.setAmount(new BigDecimal("2000000"));
            request.setTermMonths(months);

            LoanSimulatorResponse result = loanService.simulate(request);
            assertThat(result.getPaymentSchedule()).hasSize(months);
        }
    }
}
