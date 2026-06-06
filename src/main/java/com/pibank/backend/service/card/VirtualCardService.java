package com.pibank.backend.service.card;

import com.pibank.backend.domain.entity.Account;
import com.pibank.backend.domain.entity.User;
import com.pibank.backend.domain.entity.VirtualCard;
import com.pibank.backend.domain.enums.CardStatus;
import com.pibank.backend.domain.enums.CardType;
import com.pibank.backend.dto.response.VirtualCardResponse;
import com.pibank.backend.exception.*;
import com.pibank.backend.repository.UserRepository;
import com.pibank.backend.repository.VirtualCardRepository;
import com.pibank.backend.service.account.AccountService;
import com.pibank.backend.util.CvvGenerator;
import com.pibank.backend.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VirtualCardService {

    private final VirtualCardRepository virtualCardRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final EncryptionUtil encryptionUtil;
    private final CvvGenerator cvvGenerator;

    @Value("${pibank.virtual-card.cvv-rotation-minutes}")
    private int cvvRotationMinutes;

    @Transactional
    public VirtualCardResponse createVirtualCard(String accountId, String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        Account account = accountService.findById(accountId);
        accountService.validateAccountActive(account);

        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        String rawCardNumber = cvvGenerator.generateCardNumber();
        String rawCvv = cvvGenerator.generate();
        YearMonth expiry = YearMonth.now().plusYears(3);

        VirtualCard card = VirtualCard.builder()
            .account(account)
            .user(user)
            .cardNumberEncrypted(encryptionUtil.encrypt(rawCardNumber))
            .lastFourDigits(rawCardNumber.substring(rawCardNumber.length() - 4))
            .cardType(CardType.VIRTUAL_DEBIT)
            .status(CardStatus.ACTIVE)
            .expiryYearMonth(expiry.toString())
            .cvvEncrypted(encryptionUtil.encrypt(rawCvv))
            .cvvLastRotatedAt(LocalDateTime.now())
            .cardHolderName(user.getFullName().toUpperCase())
            .alias("Tarjeta Virtual")
            .build();

        card = virtualCardRepository.save(card);
        log.info("Tarjeta virtual creada para usuario: {}", user.getEmail());

        return buildResponse(card, rawCvv);
    }

    @Transactional(readOnly = true)
    public VirtualCardResponse getCardDetails(String cardId, String userId) {
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("Tarjeta", "id", cardId));

        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new PibankException("La tarjeta no está activa",
                org.springframework.http.HttpStatus.FORBIDDEN, "CARD_NOT_ACTIVE");
        }

        String currentCvv = encryptionUtil.decrypt(card.getCvvEncrypted());
        LocalDateTime cvvExpiresAt = card.getCvvLastRotatedAt().plusMinutes(cvvRotationMinutes);

        return buildResponse(card, currentCvv);
    }

    @Transactional(readOnly = true)
    public List<VirtualCardResponse> getCardsByUserId(String userId) {
        return virtualCardRepository.findAllByUserId(userId)
            .stream()
            .map(card -> buildResponse(card, null))
            .collect(Collectors.toList());
    }

    @Transactional
    public VirtualCardResponse blockCard(String cardId, String userId, String reason) {
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("Tarjeta", "id", cardId));

        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        card.setStatus(CardStatus.BLOCKED);
        card.setBlockedReason(reason);
        return buildResponse(virtualCardRepository.save(card), null);
    }

    @Scheduled(fixedDelay = 60_000) // Cada minuto
    @Transactional
    public void rotateCvvs() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(cvvRotationMinutes);
        List<VirtualCard> cardsToRotate = virtualCardRepository.findCardsNeedingCvvRotation(threshold);

        for (VirtualCard card : cardsToRotate) {
            String newCvv = cvvGenerator.generate();
            card.setCvvEncrypted(encryptionUtil.encrypt(newCvv));
            card.setCvvLastRotatedAt(LocalDateTime.now());
            virtualCardRepository.save(card);
        }

        if (!cardsToRotate.isEmpty()) {
            log.debug("CVV rotado para {} tarjetas virtuales", cardsToRotate.size());
        }
    }

    private VirtualCardResponse buildResponse(VirtualCard card, String plainCvv) {
        LocalDateTime cvvExpiresAt = card.getCvvLastRotatedAt().plusMinutes(cvvRotationMinutes);
        return VirtualCardResponse.builder()
            .id(card.getId())
            .lastFourDigits(card.getLastFourDigits())
            .cardType(card.getCardType())
            .status(card.getStatus())
            .expiryYearMonth(card.getExpiryYearMonth())
            .cvv(plainCvv)
            .cvvExpiresAt(cvvExpiresAt)
            .alias(card.getAlias())
            .cardHolderName(card.getCardHolderName())
            .accountNumber(card.getAccount().getAccountNumber())
            .createdAt(card.getCreatedAt())
            .build();
    }
}
