package com.pibank.backend.repository;

import com.pibank.backend.domain.entity.TermsAndConditions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermsAndConditionsRepository extends JpaRepository<TermsAndConditions, String> {

    Optional<TermsAndConditions> findByIsActiveTrue();

    Optional<TermsAndConditions> findByVersion(String version);

    boolean existsByVersion(String version);
}
