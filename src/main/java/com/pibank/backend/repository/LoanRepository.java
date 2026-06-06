package com.pibank.backend.repository;

import com.pibank.backend.domain.entity.Loan;
import com.pibank.backend.domain.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, String> {

    Optional<Loan> findByLoanNumber(String loanNumber);

    List<Loan> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId AND l.status IN ('ACTIVE', 'APPROVED')")
    List<Loan> findActiveByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user.id = :userId AND l.status = 'ACTIVE'")
    long countActiveByUserId(@Param("userId") String userId);

    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.nextPaymentDate < CURRENT_DATE")
    List<Loan> findOverdueLoans();
}
