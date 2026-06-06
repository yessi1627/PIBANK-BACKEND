package com.pibank.backend.controller;

import com.pibank.backend.dto.response.AccountResponse;
import com.pibank.backend.dto.response.ApiResponse;
import com.pibank.backend.repository.UserRepository;
import com.pibank.backend.service.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Cuentas", description = "Gestión de cuentas bancarias")
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Listar mis cuentas", description = "Retorna todas las cuentas del usuario autenticado.")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        List<AccountResponse> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Ver detalle de cuenta")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @PathVariable String accountId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        boolean isAdmin = userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        AccountResponse account = accountService.getAccountById(accountId, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @PatchMapping("/{accountId}/rounding")
    @Operation(summary = "Activar/desactivar redondeo automático", description = "Activa el redondeo para enviar los centavos de cambio a un bolsillo de ahorro.")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<AccountResponse>> toggleRounding(
            @PathVariable String accountId,
            @RequestParam boolean enabled,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        AccountResponse account = accountService.toggleRounding(accountId, userId, enabled);
        String msg = enabled ? "Redondeo automático activado" : "Redondeo automático desactivado";
        return ResponseEntity.ok(ApiResponse.success(account, msg));
    }

    private String getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow().getId();
    }
}
