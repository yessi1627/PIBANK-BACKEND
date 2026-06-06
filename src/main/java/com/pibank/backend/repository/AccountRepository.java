package com.pibank.backend.repository;

import com.pibank.backend.domain.entity.Account;
import com.pibank.backend.domain.enums.AccountStatus;
import com.pibank.backend.domain.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserIdAndStatus(String userId, AccountStatus status);

    List<Account> findByUserId(String userId);

    List<Account> findByUserIdAndAccountType(String userId, AccountType accountType);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.accountType = :type AND a.status = 'ACTIVE'")
    Optional<Account> findActiveByUserIdAndType(
        @Param("userId") String userId,
        @Param("type") AccountType type
    );

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.user.id = :userId AND a.status = 'ACTIVE'")
    BigDecimal sumBalanceByUserId(@Param("userId") String userId);

    @Query("SELECT a FROM Account a WHERE a.user.phoneNumber = :phone AND a.status = 'ACTIVE' AND a.accountType != 'CREDIT'")
    Optional<Account> findActiveByUserPhone(@Param("phone") String phone);
}
