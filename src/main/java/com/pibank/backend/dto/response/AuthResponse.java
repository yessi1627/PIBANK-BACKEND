package com.pibank.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;
    private Boolean mfaRequired;
    private String mfaToken;

    public static AuthResponse mfaRequired(String mfaToken) {
        return AuthResponse.builder()
            .mfaRequired(true)
            .mfaToken(mfaToken)
            .tokenType("Bearer")
            .build();
    }
}
