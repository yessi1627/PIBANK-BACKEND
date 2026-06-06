package com.pibank.backend.controller;

import com.pibank.backend.dto.request.TransferRequest;
import com.pibank.backend.dto.response.ApiResponse;
import com.pibank.backend.dto.response.TransactionResponse;
import com.pibank.backend.repository.UserRepository;
import com.pibank.backend.service.transaction.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Transacciones", description = "Transferencias, pagos e historial de movimientos")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping("/transfer")
    @Operation(summary = "Realizar transferencia", description = "Transfiere dinero a otra cuenta por número de cuenta o número de celular.")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        String userId = getUserId(userDetails);
        String ip = getClientIp(httpRequest);
        String device = httpRequest.getHeader("User-Agent");

        TransactionResponse result = transactionService.transfer(request, userId, ip, device);
        return ResponseEntity.ok(ApiResponse.success(result, "Transferencia realizada exitosamente"));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Historial de cuenta", description = "Retorna el historial de movimientos de una cuenta con paginación.")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getByAccount(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<TransactionResponse> transactions = transactionService.getTransactionsByAccount(
            accountId, PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/my")
    @Operation(summary = "Mis transacciones", description = "Historial de todas las transacciones del usuario autenticado.")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getMyTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String userId = getUserId(userDetails);
        Page<TransactionResponse> transactions = transactionService.getTransactionsByUser(
            userId, PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    private String getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow().getId();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwarded = request.getHeader("X-Forwarded-For");
        return xForwarded != null ? xForwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
