package com.pibank.backend.service.auth;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MfaService {

    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    private static final String ISSUER = "PiBank";

    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    public String generateQrUrl(String email, String secret) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(ISSUER, email,
            new GoogleAuthenticatorKey.Builder(secret).build());
    }

    public boolean verifyCode(String secret, int code) {
        try {
            return googleAuthenticator.authorize(secret, code);
        } catch (Exception e) {
            log.warn("Error verificando código TOTP: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifyCode(String secret, String codeStr) {
        try {
            int code = Integer.parseInt(codeStr);
            return verifyCode(secret, code);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
