package com.pibank.backend.repository;

import com.pibank.backend.domain.entity.AtmCode;
import com.pibank.backend.domain.enums.AtmCodeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AtmCodeRepository extends JpaRepository<AtmCode, String> {

    List<AtmCode> findByUserIdAndStatus(String userId, AtmCodeStatus status);

    @Query("SELECT a FROM AtmCode a WHERE a.user.id = :userId AND a.status = 'PENDING' AND a.expiresAt > :now")
    List<AtmCode> findActiveCodesByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE AtmCode a SET a.status = 'EXPIRED' WHERE a.status = 'PENDING' AND a.expiresAt < :now")
    int expireOldCodes(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(a) FROM AtmCode a WHERE a.user.id = :userId AND a.status = 'PENDING' AND a.expiresAt > :now")
    long countActiveCodesByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);
}
