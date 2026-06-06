package com.pibank.backend.service.savings;

import com.pibank.backend.domain.entity.Account;
import com.pibank.backend.domain.entity.SavingPocket;
import com.pibank.backend.domain.entity.User;
import com.pibank.backend.dto.request.SavingPocketRequest;
import com.pibank.backend.dto.response.SavingPocketResponse;
import com.pibank.backend.exception.ResourceNotFoundException;
import com.pibank.backend.exception.UnauthorizedException;
import com.pibank.backend.repository.AccountRepository;
import com.pibank.backend.repository.SavingPocketRepository;
import com.pibank.backend.repository.UserRepository;
import com.pibank.backend.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingPocketService {

    private final SavingPocketRepository savingPocketRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;

    @Transactional
    public SavingPocketResponse create(SavingPocketRequest request, String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        Account account = accountService.findById(request.getAccountId());

        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        SavingPocket pocket = SavingPocket.builder()
            .user(user)
            .account(account)
            .name(request.getName())
            .emoji(request.getEmoji())
            .targetAmount(request.getTargetAmount())
            .targetDate(request.getTargetDate())
            .roundingEnabled(request.getRoundingEnabled() != null && request.getRoundingEnabled())
            .roundingMultiplier(request.getRoundingMultiplier() != null
                ? request.getRoundingMultiplier() : BigDecimal.ONE)
            .isActive(true)
            .isCompleted(false)
            .build();

        return mapToResponse(savingPocketRepository.save(pocket));
    }

    @Transactional(readOnly = true)
    public List<SavingPocketResponse> getByUserId(String userId) {
        return savingPocketRepository.findByUserIdAndIsActiveTrue(userId)
            .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void applyRounding(Account fromAccount, BigDecimal purchaseAmount) {
        if (!fromAccount.getRoundingEnabled()) return;

        List<SavingPocket> pockets = savingPocketRepository
            .findRoundingEnabledByAccountId(fromAccount.getId());

        if (pockets.isEmpty()) return;

        BigDecimal roundedUp = purchaseAmount.setScale(0, RoundingMode.CEILING)
            .multiply(new BigDecimal("1000")).setScale(0, RoundingMode.CEILING);
        BigDecimal remainder = roundedUp.subtract(purchaseAmount).abs();

        if (remainder.compareTo(BigDecimal.ZERO) <= 0) return;

        SavingPocket pocket = pockets.get(0);

        if (fromAccount.getAvailableBalance().compareTo(remainder) < 0) return;

        fromAccount.setBalance(fromAccount.getBalance().subtract(remainder));
        fromAccount.setAvailableBalance(fromAccount.getAvailableBalance().subtract(remainder));
        accountRepository.save(fromAccount);

        pocket.setCurrentAmount(pocket.getCurrentAmount().add(remainder));
        if (pocket.getTargetAmount() != null &&
            pocket.getCurrentAmount().compareTo(pocket.getTargetAmount()) >= 0) {
            pocket.setIsCompleted(true);
        }
        savingPocketRepository.save(pocket);

        log.debug("Redondeo aplicado: ${} al bolsillo '{}'", remainder, pocket.getName());
    }

    public SavingPocketResponse mapToResponse(SavingPocket p) {
        return SavingPocketResponse.builder()
            .id(p.getId())
            .name(p.getName())
            .emoji(p.getEmoji())
            .targetAmount(p.getTargetAmount())
            .currentAmount(p.getCurrentAmount())
            .progressPercentage(p.getProgressPercentage())
            .targetDate(p.getTargetDate())
            .roundingEnabled(p.getRoundingEnabled())
            .roundingMultiplier(p.getRoundingMultiplier())
            .isActive(p.getIsActive())
            .isCompleted(p.getIsCompleted())
            .createdAt(p.getCreatedAt())
            .build();
    }
}
