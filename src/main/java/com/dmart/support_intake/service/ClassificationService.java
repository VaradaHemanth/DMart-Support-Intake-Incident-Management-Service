package com.dmart.support_intake.service;

import com.dmart.support_intake.dto.EventRequest;
import com.dmart.support_intake.entity.SignatureRegistry;
import com.dmart.support_intake.exception.CategoryNotFoundException;
import com.dmart.support_intake.repository.SignatureRegistryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClassificationService {

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

        // 3. DB లొ లేకపోతే ఎక్సెప్షన్ త్రో చేయడం (Failed Request)
        throw new CategoryNotFoundException(
            "Category not present in database registry for integrationName: '" 
            + request.getIntegrationName() + "' or errorCode: '" + request.getErrorCode() + "'"
        );
    }
}