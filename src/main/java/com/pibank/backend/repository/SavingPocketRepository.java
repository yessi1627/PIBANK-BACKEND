package com.pibank.backend.repository;

import com.pibank.backend.domain.entity.SavingPocket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SavingPocketRepository extends JpaRepository<SavingPocket, String> {

    List<SavingPocket> findByUserIdAndIsActiveTrue(String userId);

    List<SavingPocket> findByAccountIdAndIsActiveTrue(String accountId);

    @Query("SELECT COALESCE(SUM(p.currentAmount), 0) FROM SavingPocket p WHERE p.user.id = :userId AND p.isActive = true")
    BigDecimal sumSavingsByUserId(@Param("userId") String userId);

    @Query("SELECT p FROM SavingPocket p WHERE p.account.id = :accountId AND p.roundingEnabled = true AND p.isActive = true")
    List<SavingPocket> findRoundingEnabledByAccountId(@Param("accountId") String accountId);
}
