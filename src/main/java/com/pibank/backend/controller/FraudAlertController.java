package com.pibank.backend.controller;

import com.pibank.backend.domain.enums.AlertSeverity;
import com.pibank.backend.domain.enums.AlertStatus;
import com.pibank.backend.dto.response.ApiResponse;
import com.pibank.backend.exception.ResourceNotFoundException;
import com.pibank.backend.repository.FraudAlertRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fraud")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Detección de Fraude", description = "Gestión de alertas de fraude — acceso ADMIN y ANALYST")
@PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
public class FraudAlertController {

    private final FraudAlertRepository fraudAlertRepository;

    @GetMapping("/alerts")
    @Operation(summary = "Listar alertas abiertas", description = "Retorna todas las alertas con status OPEN paginadas.")
    public ResponseEntity<ApiResponse<?>> getOpenAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
            fraudAlertRepository.findByStatus(AlertStatus.OPEN, PageRequest.of(page, size))
        ));
    }

    @GetMapping("/alerts/critical")
    @Operation(summary = "Alertas críticas", description = "Lista alertas de severidad CRITICAL sin resolver.")
    public ResponseEntity<ApiResponse<?>> getCriticalAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
            fraudAlertRepository.findBySeverityAndStatus(
                AlertSeverity.CRITICAL, AlertStatus.OPEN, PageRequest.of(page, size))
        ));
    }

    @PatchMapping("/alerts/{alertId}/resolve")
    @Operation(summary = "Resolver alerta", description = "Marca una alerta como resuelta o falso positivo.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> resolveAlert(
            @PathVariable String alertId,
            @RequestParam AlertStatus resolution) {

        if (resolution != AlertStatus.RESOLVED && resolution != AlertStatus.FALSE_POSITIVE) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("El estado debe ser RESOLVED o FALSE_POSITIVE", "INVALID_RESOLUTION"));
        }

        var alert = fraudAlertRepository.findById(alertId)
            .orElseThrow(() -> new ResourceNotFoundException("Alerta", "id", alertId));

        alert.setStatus(resolution);
        fraudAlertRepository.save(alert);

        return ResponseEntity.ok(ApiResponse.successMessage("Alerta actualizada a: " + resolution));
    }

    @GetMapping("/stats")
    @Operation(summary = "Estadísticas de fraude", description = "Número total de alertas pendientes de revisión.")
    public ResponseEntity<ApiResponse<?>> getStats() {
        long pending = fraudAlertRepository.countPendingAlerts();
        return ResponseEntity.ok(ApiResponse.success(
            java.util.Map.of("alertasPendientes", pending)
        ));
    }
}
