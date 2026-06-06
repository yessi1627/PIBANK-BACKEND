package com.pibank.backend.repository;

import com.pibank.backend.domain.entity.Investment;
import com.pibank.backend.domain.enums.InvestmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, String> {

    Optional<Investment> findByInvestmentNumber(String investmentNumber);

    List<Investment> findByUserIdAndStatus(String userId, InvestmentStatus status);

    List<Investment> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT COALESCE(SUM(i.currentValue), 0) FROM Investment i WHERE i.user.id = :userId AND i.status = 'ACTIVE'")
    BigDecimal sumCurrentValueByUserId(@Param("userId") String userId);

    @Query("SELECT i FROM Investment i WHERE i.status = 'ACTIVE' AND i.maturityDate <= :today AND i.autoReinvest = false")
    List<Investment> findMaturedInvestments(@Param("today") LocalDate today);
}
