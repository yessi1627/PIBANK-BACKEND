package com.pibank.backend.util;

import com.pibank.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class AccountNumberGenerator {

    private static final String PREFIX = "PI";
    private static final SecureRandom random = new SecureRandom();
    private final AccountRepository accountRepository;

    public String generate() {
        String number;
        do {
            number = PREFIX + String.format("%014d", Math.abs(random.nextLong() % 100_000_000_000_000L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    public String generateLoanNumber() {
        return "LN" + String.format("%012d", Math.abs(random.nextLong() % 1_000_000_000_000L));
    }

    public String generateTransactionNumber() {
        return "TX" + System.currentTimeMillis() + String.format("%04d", random.nextInt(10000));
    }

    public String generateInvestmentNumber() {
        return "INV" + String.format("%011d", Math.abs(random.nextLong() % 100_000_000_000L));
    }
}
