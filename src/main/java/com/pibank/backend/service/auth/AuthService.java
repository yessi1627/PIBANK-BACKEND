package com.pibank.backend.service.auth;

import com.pibank.backend.domain.entity.*;
import com.pibank.backend.domain.enums.*;
import com.pibank.backend.dto.request.*;
import com.pibank.backend.dto.response.*;
import com.pibank.backend.exception.*;
import com.pibank.backend.repository.*;
import com.pibank.backend.service.account.AccountService;
import com.pibank.backend.util.AccountNumberGenerator;
import com.pibank.backend.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TermsAndConditionsRepository termsRepository;
    private final JwtService jwtService;
    private final MfaService mfaService;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AccountService accountService;
    private final EncryptionUtil encryptionUtil;

    @Value("${pibank.security.max-failed-login-attempts}")
    private int maxFailedAttempts;

    @Value("${pibank.security.account-lock-duration-minutes}")
    private int lockDurationMinutes;

    @Value("${pibank.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String deviceInfo) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Credenciales incorrectas"));

        if (user.isAccountLocked()) {
            throw new PibankException(
                "Cuenta bloqueada temporalmente. Intente de nuevo en " + lockDurationMinutes + " minutos.",
                org.springframework.http.HttpStatus.LOCKED, "ACCOUNT_LOCKED"
            );
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AccountBlockedException(user.getEmail());
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new PibankException("Cuenta suspendida. Contacte a soporte.", org.springframework.http.HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED");
        }

        if (!user.getEmailVerified()) {
            throw new PibankException("Debe verificar su email antes de iniciar sesión.", org.springframework.http.HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED");
        }

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception ex) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Credenciales incorrectas");
        }

        userRepository.resetFailedLoginAttempts(user.getId());
        userRepository.updateLastLogin(user.getId(), LocalDateTime.now(), ipAddress);

        if (user.getMfaEnabled()) {
            String mfaToken = jwtService.generateMfaToken(user.getEmail(), user.getId());
            log.info("MFA requerido para usuario: {}", user.getEmail());
            return AuthResponse.mfaRequired(mfaToken);
        }

        return buildFullAuthResponse(user, ipAddress, deviceInfo);
    }

    @Transactional
    public AuthResponse verifyMfa(MfaVerifyRequest request, String ipAddress, String deviceInfo) {
        if (!jwtService.isMfaToken(request.getMfaToken())) {
            throw new InvalidTokenException("Token MFA inválido");
        }

        String email = jwtService.extractUsername(request.getMfaToken());
        String userId = jwtService.extractUserId(request.getMfaToken());

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));

        String decryptedSecret = encryptionUtil.decrypt(user.getMfaSecret());
        if (!mfaService.verifyCode(decryptedSecret, request.getTotpCode())) {
            throw new UnauthorizedException("Código MFA incorrecto");
        }

        return buildFullAuthResponse(user, ipAddress, deviceInfo);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new PibankException("El email ya está registrado", org.springframework.http.HttpStatus.CONFLICT, "EMAIL_EXISTS");
        }
        if (userRepository.existsByDocumentNumber(request.getDocumentNumber())) {
            throw new PibankException("El documento ya está registrado", org.springframework.http.HttpStatus.CONFLICT, "DOCUMENT_EXISTS");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PibankException("Las contraseñas no coinciden", org.springframework.http.HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH");
        }

        User user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .documentType(request.getDocumentType())
            .documentNumber(request.getDocumentNumber())
            .birthDate(request.getBirthDate())
            .address(request.getAddress())
            .city(request.getCity())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(UserRole.CUSTOMER)
            .status(UserStatus.PENDING_VERIFICATION)
            .emailVerified(false)
            .termsAccepted(true)
            .termsAcceptedAt(LocalDateTime.now())
            .build();

        user = userRepository.save(user);
        accountService.createDefaultAccount(user);
        log.info("Nuevo usuario registrado: {}", user.getEmail());

        return mapToUserResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String deviceInfo) {
        String tokenHash = hashToken(request.getRefreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new InvalidTokenException("Refresh token inválido"));

        if (!stored.isValid()) {
            throw new InvalidTokenException("Refresh token expirado o revocado");
        }

        User user = stored.getUser();
        stored.setRevoked(true);
        stored.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(stored);

        return buildFullAuthResponse(user, ipAddress, deviceInfo);
    }

    @Transactional
    public void logout(String userId) {
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
        log.info("Usuario desconectado: {}", userId);
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new PibankException("La contraseña actual es incorrecta", org.springframework.http.HttpStatus.BAD_REQUEST, "WRONG_PASSWORD");
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new PibankException("Las nuevas contraseñas no coinciden", org.springframework.http.HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
        log.info("Contraseña cambiada para usuario: {}", user.getEmail());
    }

    @Transactional
    public String enableMfa(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        String secret = mfaService.generateSecret();
        user.setMfaSecret(encryptionUtil.encrypt(secret));
        userRepository.save(user);

        return mfaService.generateQrUrl(user.getEmail(), secret);
    }

    @Transactional
    public void confirmMfaSetup(String userId, String totpCode) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        String decryptedSecret = encryptionUtil.decrypt(user.getMfaSecret());
        if (!mfaService.verifyCode(decryptedSecret, totpCode)) {
            throw new UnauthorizedException("Código MFA incorrecto. No se pudo activar el MFA.");
        }

        user.setMfaEnabled(true);
        userRepository.save(user);
        log.info("MFA activado para usuario: {}", user.getEmail());
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new InvalidTokenException("Token de verificación inválido o expirado"));

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
        log.info("Email verificado para usuario: {}", user.getEmail());
    }

    private void handleFailedLogin(User user) {
        userRepository.incrementFailedLoginAttempts(user.getId());
        int attempts = user.getFailedLoginAttempts() + 1;
        if (attempts >= maxFailedAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
            user.setStatus(UserStatus.BLOCKED);
            userRepository.save(user);
            log.warn("Cuenta bloqueada por intentos fallidos: {}", user.getEmail());
        }
    }

    private AuthResponse buildFullAuthResponse(User user, String ipAddress, String deviceInfo) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails, user.getId(), user.getRole().name());
        String rawRefreshToken = jwtService.generateRefreshToken(userDetails, user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .tokenHash(hashToken(rawRefreshToken))
            .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
            .ipAddress(ipAddress)
            .deviceInfo(deviceInfo)
            .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(rawRefreshToken)
            .tokenType("Bearer")
            .expiresIn(900L)
            .mfaRequired(false)
            .user(mapToUserResponse(user))
            .build();
    }

    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear token", e);
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .documentType(user.getDocumentType())
            .documentNumber(user.getDocumentNumber())
            .birthDate(user.getBirthDate())
            .role(user.getRole())
            .status(user.getStatus())
            .mfaEnabled(user.getMfaEnabled())
            .emailVerified(user.getEmailVerified())
            .termsAccepted(user.getTermsAccepted())
            .termsAcceptedAt(user.getTermsAcceptedAt())
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
