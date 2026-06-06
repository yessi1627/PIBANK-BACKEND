package com.pibank.backend.service.account;

import com.pibank.backend.domain.entity.Account;
import com.pibank.backend.domain.entity.User;
import com.pibank.backend.domain.enums.AccountStatus;
import com.pibank.backend.domain.enums.AccountType;
import com.pibank.backend.dto.response.AccountResponse;
import com.pibank.backend.exception.AccountBlockedException;
import com.pibank.backend.exception.ResourceNotFoundException;
import com.pibank.backend.exception.UnauthorizedException;
import com.pibank.backend.repository.AccountRepository;
import com.pibank.backend.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator numberGenerator;

    @Transactional
    public Account createDefaultAccount(User user) {
        Account account = Account.builder()
            .user(user)
            .accountNumber(numberGenerator.generate())
            .accountType(AccountType.SAVINGS)
            .status(AccountStatus.ACTIVE)
            .balance(BigDecimal.ZERO)
            .availableBalance(BigDecimal.ZERO)
            .currency("COP")
            .alias("Mi Cuenta")
            .roundingEnabled(false)
            .build();

        log.info("Cuenta de ahorros creada para usuario: {}", user.getEmail());
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(String userId) {
        return accountRepository.findByUserId(userId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(String accountId, String requestingUserId, boolean isAdmin) {
        Account account = findById(accountId);
        if (!isAdmin && !account.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedException("No tiene permiso para ver esta cuenta");
        }
        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public Account findById(String accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Cuenta", "id", accountId));
    }

    @Transactional(readOnly = true)
    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Cuenta", "número", accountNumber));
    }

    public void validateAccountActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountBlockedException(account.getAccountNumber());
        }
    }

    @Transactional
    public AccountResponse toggleRounding(String accountId, String userId, boolean enabled) {
        Account account = findById(accountId);
        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }
        account.setRoundingEnabled(enabled);
        return mapToResponse(accountRepository.save(account));
    }

    public AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
            .id(account.getId())
            .accountNumber(account.getAccountNumber())
            .accountType(account.getAccountType())
            .status(account.getStatus())
            .balance(account.getBalance())
            .availableBalance(account.getAvailableBalance())
            .currency(account.getCurrency())
            .alias(account.getAlias())
            .creditLimit(account.getCreditLimit())
            .interestRate(account.getInterestRate())
            .cutoffDay(account.getCutoffDay())
            .paymentDay(account.getPaymentDay())
            .roundingEnabled(account.getRoundingEnabled())
            .lastMovementAt(account.getLastMovementAt())
            .createdAt(account.getCreatedAt())
            .build();
    }
}
