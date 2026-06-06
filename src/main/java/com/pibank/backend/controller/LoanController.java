package com.pibank.backend.controller;

import com.pibank.backend.dto.request.LoanRequest;
import com.pibank.backend.dto.request.LoanSimulatorRequest;
import com.pibank.backend.dto.response.ApiResponse;
import com.pibank.backend.dto.response.LoanResponse;
import com.pibank.backend.dto.response.LoanSimulatorResponse;
import com.pibank.backend.repository.UserRepository;
import com.pibank.backend.service.loan.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Créditos", description = "Simulador de crédito y solicitud de préstamos")
public class LoanController {

    private final LoanService loanService;
    private final UserRepository userRepository;

    @PostMapping("/simulate")
    @Operation(summary = "Simular crédito", description = "Calcula la cuota mensual, total a pagar, intereses y tabla de amortización sin comprometer nada.")
    public ResponseEntity<ApiResponse<LoanSimulatorResponse>> simulate(
            @Valid @RequestBody LoanSimulatorRequest request) {

        LoanSimulatorResponse simulation = loanService.simulate(request);
        return ResponseEntity.ok(ApiResponse.success(simulation, "Simulación de crédito calculada"));
    }

    @PostMapping("/apply")
    @Operation(summary = "Solicitar crédito", description = "Envía una solicitud formal de crédito. El sistema evalúa el score crediticio automáticamente.")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<LoanResponse>> apply(
            @Valid @RequestBody LoanRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        LoanResponse loan = loanService.apply(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(loan, "Solicitud de crédito enviada exitosamente"));
    }

    @GetMapping("/my")
    @Operation(summary = "Mis créditos", description = "Lista todos los créditos del usuario autenticado.")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getMyLoans(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(loanService.getLoansByUserId(userId)));
    }

    @GetMapping("/pending")
    @Operation(summary = "Créditos pendientes de aprobación", description = "Solo ADMIN puede ver y aprobar solicitudes pendientes.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
            loanService.getPendingLoans(PageRequest.of(page, size))
        ));
    }

    private String getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow().getId();
    }
}
