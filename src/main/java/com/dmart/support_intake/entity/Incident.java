package com.dmart.support_intake.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String signatureHash;
    private String jiraKey;
    private String integrationName;
    private String node;
    private String errorCode;
    private String env;
    
    // కొత్తగా ఈ ఫీల్డ్ ని యాడ్ చేయండి
    private String orderNo;

    private String status;
    private String classifierTier;
    private int occurrenceCount;

    private Instant firstDetectedAt;
    private Instant lastDetectedAt;
    private Instant statusChangedAt;

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSignatureHash() { return signatureHash; }
    public void setSignatureHash(String signatureHash) { this.signatureHash = signatureHash; }

    public String getJiraKey() { return jiraKey; }
    public void setJiraKey(String jiraKey) { this.jiraKey = jiraKey; }

    public String getIntegrationName() { return integrationName; }
    public void setIntegrationName(String integrationName) { this.integrationName = integrationName; }

    public String getNode() { return node; }
    public void setNode(String node) { this.node = node; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getEnv() { return env; }
    public void setEnv(String env) { this.env = env; }


    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getClassifierTier() { return classifierTier; }
    public void setClassifierTier(String classifierTier) { this.classifierTier = classifierTier; }

    public int getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(int occurrenceCount) { this.occurrenceCount = occurrenceCount; }

    public Instant getFirstDetectedAt() { return firstDetectedAt; }
    public void setFirstDetectedAt(Instant firstDetectedAt) { this.firstDetectedAt = firstDetectedAt; }

    public Instant getLastDetectedAt() { return lastDetectedAt; }
    public void setLastDetectedAt(Instant lastDetectedAt) { this.lastDetectedAt = lastDetectedAt; }

    public Instant getStatusChangedAt() { return statusChangedAt; }
    public void setStatusChangedAt(Instant statusChangedAt) { this.statusChangedAt = statusChangedAt; }
}