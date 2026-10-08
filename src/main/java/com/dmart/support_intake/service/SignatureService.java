package com.dmart.support_intake.service;

import com.dmart.support_intake.dto.EventRequest;
import com.dmart.support_intake.util.HashUtil;
import org.springframework.stereotype.Service;

@Service
public class SignatureService {

    public String generateSignature(EventRequest request) {
        String rawSignature =
                normalize(request.getIntegrationName())
                + "|"
                + normalize(request.getNode())
                + "|"
                + normalize(request.getErrorCode())
                + "|"
                + normalize(request.getEnv());

        return HashUtil.sha256(rawSignature);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }
}