package com.dmart.support_intake.service;

import com.dmart.support_intake.dto.EventRequest;
import com.dmart.support_intake.dto.EventResponse;
import com.dmart.support_intake.entity.Incident;
import com.dmart.support_intake.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final SignatureService signatureService;
    private final ClassificationService classificationService;
    private final JiraService jiraService;
    private final IncidentRepository incidentRepository;

    public EventService(
            SignatureService signatureService,
            ClassificationService classificationService,
            JiraService jiraService,
            IncidentRepository incidentRepository
    ) {
        this.signatureService = signatureService;
        this.classificationService = classificationService;
        this.jiraService = jiraService;
        this.incidentRepository = incidentRepository;
    }

    /**
     * Ingests, Deduplicates, Classifies, and Registers Incoming Support Events
     */
    @Transactional
    public EventResponse processEvent(EventRequest request) {
        // Step 1: Generate SHA-256 Signature Hash for Deduplication
        String signatureHash = signatureService.generateSignature(request);
        Optional<Incident> existingIncident = incidentRepository.findBySignatureHash(signatureHash);

        // Capture internal server timestamp automatically upon receipt
        Instant now = Instant.now();

        if (existingIncident.isPresent()) {
            // CASE A: DUPLICATE INCIDENT DETECTED
            Incident incident = existingIncident.get();

            // Dynamic lookup to update classifier tier if missing
            if (incident.getClassifierTier() == null || incident.getClassifierTier().trim().isEmpty()) {
                incident.setClassifierTier(classificationService.classify(request));
            }

            int newCount = incident.getOccurrenceCount() + 1;
            incident.setOccurrenceCount(newCount);

            // Updates internal timestamp for latest detection
            incident.setLastDetectedAt(now);

            incidentRepository.save(incident);

            // Append duplicate detection comment to existing Jira issue
            String comment = String.format("Duplicate error detected again on node '%s' for Order '%s' at %s. Total occurrences: %d",
                    request.getNode(), request.getOrderNo(), now.toString(), newCount);

            try {
                jiraService.addComment(incident.getJiraKey(), comment);
            } catch (Exception e) {
                // Log warning if Jira comment fails, without breaking DB transaction
                log.warn("Failed to append duplicate comment to Jira Issue {}: {}", 
                        incident.getJiraKey(), e.getMessage());
            }

            return new EventResponse(
                    "DUPLICATE_UPDATED",
                    incident.getJiraKey(),
                    true,
                    newCount
            );
        } else {
            // CASE B: NEW UNIQUE INCIDENT DETECTED

            // Dynamic Classification via Registry/Rules
            String classifierTier = classificationService.classify(request);

            // Create Jira Ticket (passes request object and derived classification tier)
            String jiraKey = jiraService.createJiraIssue(request, classifierTier);

            // Populate Incident Entity
            Incident newIncident = new Incident();
            newIncident.setSignatureHash(signatureHash);
            newIncident.setJiraKey(jiraKey);
            newIncident.setIntegrationName(request.getIntegrationName());
            newIncident.setNode(request.getNode());
            newIncident.setErrorCode(request.getErrorCode());
            newIncident.setEnv(request.getEnv());
            newIncident.setOrderNo(request.getOrderNo());

            // Initial state & timestamps
            newIncident.setStatus("OPEN");
            newIncident.setStatusChangedAt(now);
            newIncident.setClassifierTier(classifierTier);
            newIncident.setFirstDetectedAt(now);
            newIncident.setLastDetectedAt(now);
            newIncident.setOccurrenceCount(1);

            incidentRepository.save(newIncident);

            return new EventResponse(
                    "JIRA_CREATED",
                    jiraKey,
                    false,
                    1
            );
        }
    }

    /**
     * Synchronizes Incident Status Updates triggered via Jira Webhooks
     */
    @Transactional
    public boolean updateStatusFromJira(String jiraKey, String newStatus) {
        if (jiraKey == null || newStatus == null) {
            return false;
        }

        Optional<Incident> incidentOpt = incidentRepository.findByJiraKey(jiraKey);

        if (incidentOpt.isPresent()) {
            Incident incident = incidentOpt.get();

            // Stamp new timestamp only if status actually changes
            if (incident.getStatus() == null || !incident.getStatus().equalsIgnoreCase(newStatus)) {
                incident.setStatus(newStatus.toUpperCase());
                incident.setStatusChangedAt(Instant.now());
                incidentRepository.save(incident);
                return true;
            }
        }
        return false;
    }
}