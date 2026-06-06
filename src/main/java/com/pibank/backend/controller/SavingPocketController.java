package com.pibank.backend.controller;

import com.pibank.backend.dto.request.SavingPocketRequest;
import com.pibank.backend.dto.response.ApiResponse;
import com.pibank.backend.dto.response.SavingPocketResponse;
import com.pibank.backend.repository.UserRepository;
import com.pibank.backend.service.savings.SavingPocketService;
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
@RequestMapping("/saving-pockets")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Bolsillos de Ahorro", description = "Crea metas de ahorro con redondeo automático para alcanzar tus objetivos")
@PreAuthorize("hasRole('CUSTOMER')")
public class SavingPocketController {

    private final SavingPocketService savingPocketService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Crear bolsillo de ahorro", description = "Crea un bolsillo con nombre, meta y opcionalmente activa el redondeo automático.")
    public ResponseEntity<ApiResponse<SavingPocketResponse>> create(
            @Valid @RequestBody SavingPocketRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        SavingPocketResponse pocket = savingPocketService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(pocket, "Bolsillo de ahorro creado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Mis bolsillos de ahorro")
    public ResponseEntity<ApiResponse<List<SavingPocketResponse>>> getMyPockets(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(savingPocketService.getByUserId(userId)));
    }

    private String getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow().getId();
    }
}
