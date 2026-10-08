package com.dmart.support_intake.dto;

import jakarta.validation.constraints.NotBlank;

public class EventRequest {

    @NotBlank(message = "sourceChannel is required")
    private String sourceChannel;

    @NotBlank(message = "sourceId is required")
    private String sourceId;

    @NotBlank(message = "subject is required")
    private String subject;

    @NotBlank(message = "bodyText is required")
    private String bodyText;

    @NotBlank(message = "integrationName is required")
    private String integrationName;

    @NotBlank(message = "node is required")
    private String node;

    @NotBlank(message = "errorCode is required")
    private String errorCode;

    @NotBlank(message = "env is required")
    private String env;

    @NotBlank(message = "orderNo is required")
    private String orderNo;

    // Getters & Setters
    public String getSourceChannel() { return sourceChannel; }
    public void setSourceChannel(String sourceChannel) { this.sourceChannel = sourceChannel; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }

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
}