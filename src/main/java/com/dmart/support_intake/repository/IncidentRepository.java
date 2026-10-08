package com.dmart.support_intake.repository;

import com.dmart.support_intake.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findBySignatureHash(String signatureHash);
    Optional<Incident> findByJiraKey(String jiraKey);
}