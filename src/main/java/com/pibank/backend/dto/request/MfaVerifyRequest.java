package com.pibank.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MfaVerifyRequest {

    @NotBlank(message = "El token MFA temporal es requerido")
    private String mfaToken;

    @NotBlank(message = "El código TOTP es requerido")
    @Pattern(regexp = "^[0-9]{6}$", message = "El código TOTP debe tener exactamente 6 dígitos")
    private String totpCode;
}
