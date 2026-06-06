package com.pibank.backend.controller;

import com.pibank.backend.domain.entity.TermsAndConditions;
import com.pibank.backend.dto.response.ApiResponse;
import com.pibank.backend.exception.ResourceNotFoundException;
import com.pibank.backend.repository.TermsAndConditionsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
@Tag(name = "Términos y Condiciones", description = "Gestión de términos y condiciones del banco")
public class TermsController {

    private final TermsAndConditionsRepository termsRepository;

    @GetMapping("/active")
    @Operation(summary = "Ver términos y condiciones vigentes", description = "Endpoint público. Retorna los términos actuales que el usuario debe aceptar al registrarse.")
    public ResponseEntity<ApiResponse<TermsAndConditions>> getActiveTerms() {
        TermsAndConditions terms = termsRepository.findByIsActiveTrue()
            .orElseThrow(() -> new ResourceNotFoundException("No hay términos y condiciones vigentes"));
        return ResponseEntity.ok(ApiResponse.success(terms));
    }
}
