package com.pibank.backend.controller;

import com.pibank.backend.dto.response.ApiResponse;
import com.pibank.backend.dto.response.VirtualCardResponse;
import com.pibank.backend.repository.UserRepository;
import com.pibank.backend.service.card.VirtualCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/virtual-cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Tarjetas Virtuales", description = "Tarjetas virtuales con CVV dinámico que rota cada 5 minutos — para compras online seguras")
@PreAuthorize("hasRole('CUSTOMER')")
public class VirtualCardController {

    private final VirtualCardService virtualCardService;
    private final UserRepository userRepository;

    @PostMapping("/account/{accountId}")
    @Operation(summary = "Crear tarjeta virtual", description = "Genera una nueva tarjeta virtual de débito con número, fecha de expiración y CVV dinámico.")
    public ResponseEntity<ApiResponse<VirtualCardResponse>> create(
            @PathVariable String accountId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        VirtualCardResponse card = virtualCardService.createVirtualCard(accountId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(card, "Tarjeta virtual creada. ¡El CVV se rota automáticamente cada 5 minutos!"));
    }

    @GetMapping
    @Operation(summary = "Mis tarjetas virtuales")
    public ResponseEntity<ApiResponse<List<VirtualCardResponse>>> getMyCards(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(virtualCardService.getCardsByUserId(userId)));
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Ver CVV actual", description = "Muestra el CVV dinámico actual de la tarjeta. Rota automáticamente cada 5 minutos.")
    public ResponseEntity<ApiResponse<VirtualCardResponse>> getCardDetails(
            @PathVariable String cardId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        VirtualCardResponse card = virtualCardService.getCardDetails(cardId, userId);
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @PatchMapping("/{cardId}/block")
    @Operation(summary = "Bloquear tarjeta virtual", description = "Bloquea la tarjeta de forma inmediata.")
    public ResponseEntity<ApiResponse<VirtualCardResponse>> block(
            @PathVariable String cardId,
            @RequestParam(defaultValue = "Bloqueada por el usuario") String reason,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        VirtualCardResponse card = virtualCardService.blockCard(cardId, userId, reason);
        return ResponseEntity.ok(ApiResponse.success(card, "Tarjeta bloqueada exitosamente."));
    }

    private String getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow().getId();
    }
}
