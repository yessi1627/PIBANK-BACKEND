package com.pibank.backend.service.loan;

import com.pibank.backend.domain.entity.Loan;
import com.pibank.backend.domain.entity.User;
import com.pibank.backend.domain.enums.LoanStatus;
import com.pibank.backend.dto.request.LoanRequest;
import com.pibank.backend.dto.request.LoanSimulatorRequest;
import com.pibank.backend.dto.response.LoanResponse;
import com.pibank.backend.dto.response.LoanSimulatorResponse;
import com.pibank.backend.exception.*;
import com.pibank.backend.repository.LoanRepository;
import com.pibank.backend.repository.UserRepository;
import com.pibank.backend.service.account.AccountService;
import com.pibank.backend.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final AccountNumberGenerator numberGenerator;

    private static final BigDecimal DEFAULT_ANNUAL_RATE = new BigDecimal("0.2499"); // 24.99% EA
    private static final int CREDIT_SCORE_MIN = 400;

    @Transactional(readOnly = true)
    public LoanSimulatorResponse simulate(LoanSimulatorRequest request) {
        return calculateLoan(request.getAmount(), DEFAULT_ANNUAL_RATE, request.getTermMonths());
    }

    @Transactional
    public LoanResponse apply(LoanRequest request, String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        long activeLoans = loanRepository.countActiveByUserId(userId);
        if (activeLoans >= 2) {
            throw new PibankException(
                "No puede tener más de 2 créditos activos simultáneamente.",
                org.springframework.http.HttpStatus.CONFLICT, "MAX_LOANS_REACHED"
            );
        }

        var disbursementAccount = accountService.findById(request.getDisbursementAccountId());
        if (!disbursementAccount.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        // Score de crédito simulado basado en historial
        int creditScore = calculateCreditScore(user);
        LoanSimulatorResponse simulation = calculateLoan(request.getRequestedAmount(), DEFAULT_ANNUAL_RATE, request.getTermMonths());

        Loan loan = Loan.builder()
            .user(user)
            .disbursementAccount(disbursementAccount)
            .loanNumber(numberGenerator.generateLoanNumber())
            .requestedAmount(request.getRequestedAmount())
            .interestRate(DEFAULT_ANNUAL_RATE)
            .termMonths(request.getTermMonths())
            .monthlyPayment(simulation.getMonthlyPayment())
            .purpose(request.getPurpose())
            .creditScore(creditScore)
            .status(creditScore >= CREDIT_SCORE_MIN ? LoanStatus.APPROVED : LoanStatus.PENDING_REVIEW)
            .build();

        if (creditScore < CREDIT_SCORE_MIN) {
            loan.setRejectionReason("Score de crédito insuficiente: " + creditScore);
            loan.setStatus(LoanStatus.REJECTED);
        }

        loan = loanRepository.save(loan);
        log.info("Solicitud de crédito creada: {} para usuario: {}", loan.getLoanNumber(), user.getEmail());
        return mapToResponse(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByUserId(String userId) {
        return loanRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<LoanResponse> getPendingLoans(Pageable pageable) {
        return loanRepository.findByStatus(LoanStatus.PENDING_REVIEW, pageable).map(this::mapToResponse);
    }

    private LoanSimulatorResponse calculateLoan(BigDecimal amount, BigDecimal annualRate, int termMonths) {
        // Tasa mensual = (1 + tasa anual)^(1/12) - 1
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);

        // Cuota = P * (r * (1+r)^n) / ((1+r)^n - 1)
        BigDecimal pow = onePlusRate.pow(termMonths, new MathContext(10, RoundingMode.HALF_UP));
        BigDecimal numerator = amount.multiply(monthlyRate).multiply(pow);
        BigDecimal denominator = pow.subtract(BigDecimal.ONE);
        BigDecimal monthlyPayment = numerator.divide(denominator, 2, RoundingMode.HALF_UP);

        BigDecimal totalPayment = monthlyPayment.multiply(new BigDecimal(termMonths));
        BigDecimal totalInterest = totalPayment.subtract(amount);

        List<LoanSimulatorResponse.PaymentScheduleItem> schedule = new ArrayList<>();
        BigDecimal balance = amount;
        LocalDate dueDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= termMonths; i++) {
            BigDecimal interestPayment = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);
            balance = balance.subtract(principalPayment);

            schedule.add(LoanSimulatorResponse.PaymentScheduleItem.builder()
                .installmentNumber(i)
                .payment(monthlyPayment)
                .principal(principalPayment)
                .interest(interestPayment)
                .remainingBalance(balance.max(BigDecimal.ZERO))
                .dueDate(dueDate.toString())
                .build());

            dueDate = dueDate.plusMonths(1);
        }

        return LoanSimulatorResponse.builder()
            .requestedAmount(amount)
            .annualInterestRate(annualRate)
            .monthlyInterestRate(monthlyRate.setScale(6, RoundingMode.HALF_UP))
            .termMonths(termMonths)
            .monthlyPayment(monthlyPayment)
            .totalPayment(totalPayment)
            .totalInterest(totalInterest)
            .paymentSchedule(schedule)
            .build();
    }

    private int calculateCreditScore(User user) {
        // Simulación básica de score — en producción se conectaría a una agencia de crédito
        int score = 600;
        long activeLoanCount = loanRepository.countActiveByUserId(user.getId());
        score -= (int) (activeLoanCount * 50);
        return Math.max(score, 300);
    }

    public LoanResponse mapToResponse(Loan loan) {
        return LoanResponse.builder()
            .id(loan.getId())
            .loanNumber(loan.getLoanNumber())
            .requestedAmount(loan.getRequestedAmount())
            .approvedAmount(loan.getApprovedAmount())
            .outstandingBalance(loan.getOutstandingBalance())
            .interestRate(loan.getInterestRate())
            .termMonths(loan.getTermMonths())
            .monthlyPayment(loan.getMonthlyPayment())
            .purpose(loan.getPurpose())
            .creditScore(loan.getCreditScore())
            .status(loan.getStatus())
            .rejectionReason(loan.getRejectionReason())
            .approvedAt(loan.getApprovedAt())
            .disbursedAt(loan.getDisbursedAt())
            .firstPaymentDate(loan.getFirstPaymentDate())
            .nextPaymentDate(loan.getNextPaymentDate())
            .paymentsMade(loan.getPaymentsMade())
            .daysOverdue(loan.getDaysOverdue())
            .createdAt(loan.getCreatedAt())
            .build();
    }
}
