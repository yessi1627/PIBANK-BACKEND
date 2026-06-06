package com.pibank.backend.service.transaction;

import com.pibank.backend.domain.entity.*;
import com.pibank.backend.domain.enums.*;
import com.pibank.backend.dto.request.TransferRequest;
import com.pibank.backend.dto.response.TransactionResponse;
import com.pibank.backend.exception.*;
import com.pibank.backend.repository.*;
import com.pibank.backend.service.account.AccountService;
import com.pibank.backend.service.fraud.FraudDetectionService;
import com.pibank.backend.service.savings.SavingPocketService;
import com.pibank.backend.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final FraudDetectionService fraudDetectionService;
    private final SavingPocketService savingPocketService;
    private final AccountNumberGenerator numberGenerator;

    @Transactional
    public TransactionResponse transfer(TransferRequest request, String userId,
                                         String ipAddress, String deviceInfo) {
        Account fromAccount = accountService.findById(request.getFromAccountId());
        accountService.validateAccountActive(fromAccount);

        if (!fromAccount.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tiene permiso sobre esta cuenta");
        }

        if (fromAccount.getAvailableBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(fromAccount.getAvailableBalance(), request.getAmount());
        }

        // Resolver cuenta destino (número o celular)
        Account toAccount = resolveDestinationAccount(request.getToAccountNumberOrPhone());
        accountService.validateAccountActive(toAccount);

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new PibankException("No puede transferir a la misma cuenta",
                org.springframework.http.HttpStatus.BAD_REQUEST, "SAME_ACCOUNT");
        }

        // Evaluación de fraude
        BigDecimal fraudScore = fraudDetectionService.evaluateTransaction(
            fromAccount.getUser(), fromAccount, request.getAmount(), ipAddress, deviceInfo, null
        );

        // Crear transacción
        Transaction transaction = Transaction.builder()
            .transactionNumber(numberGenerator.generateTransactionNumber())
            .type(TransactionType.TRANSFER)
            .status(TransactionStatus.PENDING)
            .fromAccount(fromAccount)
            .toAccount(toAccount)
            .amount(request.getAmount())
            .currency(fromAccount.getCurrency())
            .description(request.getDescription())
            .category(request.getCategory() != null ? request.getCategory() : TransactionCategory.TRANSFER)
            .ipAddress(ipAddress)
            .deviceInfo(deviceInfo)
            .fraudScore(fraudScore)
            .build();

        transaction = transactionRepository.save(transaction);

        // Actualizar saldos
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        fromAccount.setAvailableBalance(fromAccount.getAvailableBalance().subtract(request.getAmount()));
        fromAccount.setLastMovementAt(LocalDateTime.now());

        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        toAccount.setAvailableBalance(toAccount.getAvailableBalance().add(request.getAmount()));
        toAccount.setLastMovementAt(LocalDateTime.now());

        transaction.setBalanceAfterFrom(fromAccount.getBalance());
        transaction.setBalanceAfterTo(toAccount.getBalance());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setProcessedAt(LocalDateTime.now());

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        transactionRepository.save(transaction);

        // Aplicar redondeo automático si está habilitado
        savingPocketService.applyRounding(fromAccount, request.getAmount());

        log.info("Transferencia completada: {} -> {} por ${}",
            fromAccount.getAccountNumber(), toAccount.getAccountNumber(), request.getAmount());

        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByAccount(String accountId, Pageable pageable) {
        return transactionRepository.findByAccountId(accountId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByUser(String userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable).map(this::mapToResponse);
    }

    private Account resolveDestinationAccount(String accountNumberOrPhone) {
        if (accountNumberOrPhone.startsWith("PI")) {
            return accountService.findByAccountNumber(accountNumberOrPhone);
        }
        return accountRepository.findActiveByUserPhone(accountNumberOrPhone)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No se encontró cuenta activa para el número: " + accountNumberOrPhone));
    }

    public TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
            .id(t.getId())
            .transactionNumber(t.getTransactionNumber())
            .type(t.getType())
            .status(t.getStatus())
            .fromAccountNumber(t.getFromAccount() != null ? t.getFromAccount().getAccountNumber() : null)
            .toAccountNumber(t.getToAccount() != null ? t.getToAccount().getAccountNumber() : null)
            .amount(t.getAmount())
            .currency(t.getCurrency())
            .description(t.getDescription())
            .category(t.getCategory())
            .reference(t.getReference())
            .fraudScore(t.getFraudScore())
            .failureReason(t.getFailureReason())
            .balanceAfter(t.getBalanceAfterFrom())
            .processedAt(t.getProcessedAt())
            .createdAt(t.getCreatedAt())
            .build();
    }
}
