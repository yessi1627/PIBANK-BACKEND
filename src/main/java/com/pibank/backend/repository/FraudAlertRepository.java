package com.pibank.backend.repository;

import com.pibank.backend.domain.entity.FraudAlert;
import com.pibank.backend.domain.enums.AlertSeverity;
import com.pibank.backend.domain.enums.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, String> {

    Page<FraudAlert> findByStatus(AlertStatus status, Pageable pageable);

    Page<FraudAlert> findBySeverityAndStatus(AlertSeverity severity, AlertStatus status, Pageable pageable);

    List<FraudAlert> findByUserIdAndStatusIn(String userId, List<AlertStatus> statuses);

    @Query("SELECT COUNT(f) FROM FraudAlert f WHERE f.user.id = :userId AND f.status = 'OPEN'")
    long countOpenAlertsByUserId(@Param("userId") String userId);

    @Query("SELECT f FROM FraudAlert f WHERE f.user.id = :userId AND f.createdAt >= :since ORDER BY f.createdAt DESC")
    List<FraudAlert> findRecentByUserId(@Param("userId") String userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(f) FROM FraudAlert f WHERE f.status IN ('OPEN', 'INVESTIGATING')")
    long countPendingAlerts();
}
