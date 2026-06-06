package com.pibank.backend.controller;

import com.pibank.backend.dto.request.*;
import com.pibank.backend.dto.response.ApiResponse;
import com.pibank.backend.dto.response.AuthResponse;
import com.pibank.backend.dto.response.UserResponse;
import com.pibank.backend.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de registro, login, MFA y gestión de sesión")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Retorna un JWT si las credenciales son correctas. Si el usuario tiene MFA activo, retorna un mfaToken temporal.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ip = getClientIp(httpRequest);
        String device = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.login(request, ip, device);
        return ResponseEntity.ok(ApiResponse.success(response, "Inicio de sesión exitoso"));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta CUSTOMER. Se requiere verificar el email antes de poder iniciar sesión.")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(user, "Registro exitoso. Revise su email para verificar la cuenta."));
    }

    @PostMapping("/mfa/verify")
    @Operation(summary = "Verificar código MFA", description = "Completa el login con el código TOTP de 6 dígitos (Google Authenticator).")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyMfa(
            @Valid @RequestBody MfaVerifyRequest request,
            HttpServletRequest httpRequest) {

        String ip = getClientIp(httpRequest);
        String device = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.verifyMfa(request, ip, device);
        return ResponseEntity.ok(ApiResponse.success(response, "Verificación MFA exitosa"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token de acceso", description = "Usa el refreshToken para obtener un nuevo accessToken sin volver a iniciar sesión.")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        String ip = getClientIp(httpRequest);
        String device = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.refreshToken(request, ip, device);
        return ResponseEntity.ok(ApiResponse.success(response, "Token renovado exitosamente"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca todos los refresh tokens del usuario.")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.successMessage("Sesión cerrada exitosamente"));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verificar email", description = "Activa la cuenta del usuario después del registro.")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.successMessage("Email verificado. Ya puede iniciar sesión."));
    }

    @PostMapping("/mfa/enable")
    @Operation(summary = "Activar MFA", description = "Genera el secreto TOTP y retorna la URL del QR para escanear con Google Authenticator.")
    public ResponseEntity<ApiResponse<String>> enableMfa(@AuthenticationPrincipal UserDetails userDetails) {
        String qrUrl = authService.enableMfa(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(qrUrl, "Escanee el QR con Google Authenticator y confirme con su código."));
    }

    @PostMapping("/mfa/confirm")
    @Operation(summary = "Confirmar activación MFA", description = "Confirma que el usuario escaneó el QR correctamente. A partir de aquí, cada login requerirá MFA.")
    public ResponseEntity<ApiResponse<Void>> confirmMfa(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String totpCode) {
        authService.confirmMfaSetup(userDetails.getUsername(), totpCode);
        return ResponseEntity.ok(ApiResponse.successMessage("MFA activado correctamente."));
    }

    @PutMapping("/password")
    @Operation(summary = "Cambiar contraseña")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.successMessage("Contraseña actualizada. Inicie sesión nuevamente."));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
