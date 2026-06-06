package com.pibank.backend.repository;

import com.pibank.backend.domain.entity.Transaction;
import com.pibank.backend.domain.enums.TransactionCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByTransactionNumber(String transactionNumber);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountId(@Param("accountId") String accountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId) " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountIdAndDateRange(
        @Param("accountId") String accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
           "AND t.category = :category ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountIdAndCategory(
        @Param("accountId") String accountId,
        @Param("category") TransactionCategory category,
        Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE " +
           "t.fromAccount.user.id = :userId AND t.status = 'COMPLETED' " +
           "AND t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<Transaction> findRecentByUserId(
        @Param("userId") String userId,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE " +
           "t.fromAccount.user.id = :userId AND t.status = 'COMPLETED' " +
           "AND t.type = 'PURCHASE' " +
           "AND t.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category")
    List<Object[]> sumSpendingByCategoryForUser(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE " +
           "t.fromAccount.user.id = :userId AND t.createdAt >= :since")
    long countRecentTransactionsByUser(
        @Param("userId") String userId,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE " +
           "t.fromAccount.user.id = :userId AND t.status = 'COMPLETED' " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumSpendingByUserAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
