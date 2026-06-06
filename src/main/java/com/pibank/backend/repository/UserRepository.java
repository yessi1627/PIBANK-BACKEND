package com.pibank.backend.repository;

import com.pibank.backend.domain.entity.User;
import com.pibank.backend.domain.enums.UserRole;
import com.pibank.backend.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByDocumentNumber(String documentNumber);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    boolean existsByEmail(String email);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.status = :status AND u.role = :role")
    Page<User> findByStatusAndRole(
        @Param("status") UserStatus status,
        @Param("role") UserRole role,
        Pageable pageable
    );

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :id")
    void incrementFailedLoginAttempts(@Param("id") String id);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.id = :id")
    void resetFailedLoginAttempts(@Param("id") String id);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginAt, u.lastLoginIp = :ip WHERE u.id = :id")
    void updateLastLogin(
        @Param("id") String id,
        @Param("loginAt") LocalDateTime loginAt,
        @Param("ip") String ip
    );

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.status = 'ACTIVE'")
    long countActiveByRole(@Param("role") UserRole role);
}
