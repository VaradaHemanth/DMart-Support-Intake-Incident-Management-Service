package com.dmart.support_intake.repository;

import com.dmart.support_intake.entity.SignatureRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignatureRegistryRepository extends JpaRepository<SignatureRegistry, Long> {
    
    // Pattern Name finding
    Optional<SignatureRegistry> findByPatternNameIgnoreCase(String patternName);
}