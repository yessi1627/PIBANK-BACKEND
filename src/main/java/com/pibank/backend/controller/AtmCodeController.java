package com.pibank.backend.controller;

import com.pibank.backend.dto.request.AtmCodeRequest;
import com.pibank.backend.dto.response.ApiResponse;
import com.pibank.backend.dto.response.AtmCodeResponse;
import com.pibank.backend.repository.UserRepository;
import com.pibank.backend.service.atm.AtmCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/atm-codes")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Códigos ATM", description = "Retiros sin tarjeta usando códigos temporales de seguridad")
@PreAuthorize("hasRole('CUSTOMER')")
public class AtmCodeController {

    private final AtmCodeService atmCodeService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Generar código ATM", description = "Genera un código temporal de 6 dígitos válido por 15 minutos para retirar dinero en cajero sin tarjeta.")
    public ResponseEntity<ApiResponse<AtmCodeResponse>> generate(
            @Valid @RequestBody AtmCodeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        AtmCodeResponse response = atmCodeService.generateCode(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Código ATM generado. Tiene " + response.getRemainingSeconds() + " segundos para usarlo."));
    }

    @GetMapping
    @Operation(summary = "Ver códigos activos", description = "Lista los códigos ATM pendientes. El código real está oculto por seguridad (solo se muestra al generarlo).")
    public ResponseEntity<ApiResponse<List<AtmCodeResponse>>> getActive(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(atmCodeService.getActiveCodes(userId)));
    }

    @DeleteMapping("/{codeId}")
    @Operation(summary = "Cancelar código ATM", description = "Cancela un código pendiente y devuelve el saldo reservado a la cuenta.")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable String codeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        atmCodeService.cancelCode(codeId, userId);
        return ResponseEntity.ok(ApiResponse.successMessage("Código ATM cancelado. El saldo ha sido liberado."));
    }

    private String getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow().getId();
    }
}
