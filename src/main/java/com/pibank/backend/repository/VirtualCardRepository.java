package com.pibank.backend.repository;

import com.pibank.backend.domain.entity.VirtualCard;
import com.pibank.backend.domain.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard, String> {

    List<VirtualCard> findByUserIdAndStatus(String userId, CardStatus status);

    List<VirtualCard> findByAccountIdAndStatus(String accountId, CardStatus status);

    @Query("SELECT v FROM VirtualCard v WHERE v.user.id = :userId")
    List<VirtualCard> findAllByUserId(@Param("userId") String userId);

    @Query("SELECT v FROM VirtualCard v WHERE v.status = 'ACTIVE' AND v.cvvLastRotatedAt < :threshold")
    List<VirtualCard> findCardsNeedingCvvRotation(@Param("threshold") LocalDateTime threshold);
}
