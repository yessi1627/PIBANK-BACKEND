package com.pibank.backend.repository;

import com.pibank.backend.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :revokedAt WHERE r.user.id = :userId AND r.revoked = false")
    int revokeAllByUserId(@Param("userId") String userId, @Param("revokedAt") LocalDateTime revokedAt);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :threshold OR r.revoked = true")
    int deleteExpiredAndRevoked(@Param("threshold") LocalDateTime threshold);
}
