package com.dmart.support_intake.service;

import com.dmart.support_intake.dto.EventRequest;
import com.dmart.support_intake.entity.SignatureRegistry;
import com.dmart.support_intake.repository.SignatureRegistryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClassificationService {

    private static final Logger log = LoggerFactory.getLogger(ClassificationService.class);

    private final SignatureRegistryRepository registryRepository;

    public ClassificationService(SignatureRegistryRepository registryRepository) {
        this.registryRepository = registryRepository;
    }

    public String classify(EventRequest request) {

        // 1. Check Integration Name in Signature Registry
        if (request.getIntegrationName() != null && !request.getIntegrationName().trim().isEmpty()) {
            Optional<SignatureRegistry> match = registryRepository
                    .findByPatternNameIgnoreCase(request.getIntegrationName().trim());
            if (match.isPresent()) {
                return match.get().getCategory();
            }
        }

        // 2. Check Error Code in Signature Registry
        if (request.getErrorCode() != null && !request.getErrorCode().trim().isEmpty()) {
            Optional<SignatureRegistry> match = registryRepository
                    .findByPatternNameIgnoreCase(request.getErrorCode().trim());
            if (match.isPresent()) {
                return match.get().getCategory();
            }
        }

        // 3. Fallback: Check for default integration pattern in DB
        Optional<SignatureRegistry> defaultMatch = registryRepository
                .findByPatternNameIgnoreCase("DMART_DEFAULT_INTEGRATION");
        if (defaultMatch.isPresent()) {
            log.info("No exact match found for integrationName: '{}' or errorCode: '{}'. Using database default category.", 
                    request.getIntegrationName(), request.getErrorCode());
            return defaultMatch.get().getCategory();
        }

        // 4. In-Memory Fallback: Avoid throwing exceptions if DB registry entry is missing
        log.warn("Category not present in database registry for integrationName: '{}' or errorCode: '{}'. Falling back to default 'DMART_SUPPORT_ISSUE'.", 
                request.getIntegrationName(), request.getErrorCode());

        return "DMART_SUPPORT_ISSUE";
    }
}