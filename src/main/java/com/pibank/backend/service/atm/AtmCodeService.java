package com.pibank.backend.service.atm;

import com.pibank.backend.domain.entity.Account;
import com.pibank.backend.domain.entity.AtmCode;
import com.pibank.backend.domain.entity.User;
import com.pibank.backend.domain.enums.AtmCodeStatus;
import com.pibank.backend.dto.request.AtmCodeRequest;
import com.pibank.backend.dto.response.AtmCodeResponse;
import com.pibank.backend.exception.*;
import com.pibank.backend.repository.AccountRepository;
import com.pibank.backend.repository.AtmCodeRepository;
import com.pibank.backend.repository.UserRepository;
import com.pibank.backend.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AtmCodeService {

    private final AtmCodeRepository atmCodeRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    @Value("${pibank.atm.code-length}")
    private int codeLength;

    @Value("${pibank.atm.expiration-minutes}")
    private int expirationMinutes;

    private static final SecureRandom random = new SecureRandom();
    private static final int MAX_ACTIVE_CODES = 3;

    @Transactional
    public AtmCodeResponse generateCode(AtmCodeRequest request, String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        Account account = accountService.findById(request.getAccountId());
        accountService.validateAccountActive(account);

        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        if (account.getAvailableBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(account.getAvailableBalance(), request.getAmount());
        }

        long activeCodes = atmCodeRepository.countActiveCodesByUserId(userId, LocalDateTime.now());
        if (activeCodes >= MAX_ACTIVE_CODES) {
            throw new PibankException(
                "Ya tiene " + MAX_ACTIVE_CODES + " códigos activos. Cancele uno antes de generar otro.",
                org.springframework.http.HttpStatus.CONFLICT, "MAX_CODES_REACHED"
            );
        }

        String rawCode = generateNumericCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);

        // Reservar el saldo
        account.setAvailableBalance(account.getAvailableBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        AtmCode atmCode = AtmCode.builder()
            .user(user)
            .account(account)
            .codeHash(passwordEncoder.encode(rawCode))
            .amount(request.getAmount())
            .status(AtmCodeStatus.PENDING)
            .expiresAt(expiresAt)
            .build();

        atmCode = atmCodeRepository.save(atmCode);

        log.info("Código ATM generado para usuario: {}, monto: ${}", user.getEmail(), request.getAmount());

        return AtmCodeResponse.builder()
            .id(atmCode.getId())
            .code(rawCode)
            .amount(atmCode.getAmount())
            .status(atmCode.getStatus())
            .expiresAt(atmCode.getExpiresAt())
            .accountNumber(account.getAccountNumber())
            .createdAt(atmCode.getCreatedAt())
            .remainingSeconds(ChronoUnit.SECONDS.between(LocalDateTime.now(), expiresAt))
            .build();
    }

    @Transactional(readOnly = true)
    public List<AtmCodeResponse> getActiveCodes(String userId) {
        return atmCodeRepository.findActiveCodesByUserId(userId, LocalDateTime.now())
            .stream()
            .map(code -> AtmCodeResponse.builder()
                .id(code.getId())
                .code("******")
                .amount(code.getAmount())
                .status(code.getStatus())
                .expiresAt(code.getExpiresAt())
                .accountNumber(code.getAccount().getAccountNumber())
                .createdAt(code.getCreatedAt())
                .remainingSeconds(ChronoUnit.SECONDS.between(LocalDateTime.now(), code.getExpiresAt()))
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public void cancelCode(String codeId, String userId) {
        AtmCode code = atmCodeRepository.findById(codeId)
            .orElseThrow(() -> new ResourceNotFoundException("Código ATM", "id", codeId));

        if (!code.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        if (code.getStatus() != AtmCodeStatus.PENDING) {
            throw new PibankException("Solo se pueden cancelar códigos pendientes",
                org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_CODE_STATUS");
        }

        // Devolver saldo reservado
        Account account = code.getAccount();
        account.setAvailableBalance(account.getAvailableBalance().add(code.getAmount()));
        accountRepository.save(account);

        code.setStatus(AtmCodeStatus.CANCELLED);
        atmCodeRepository.save(code);
        log.info("Código ATM cancelado: {}", codeId);
    }

    private String generateNumericCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
