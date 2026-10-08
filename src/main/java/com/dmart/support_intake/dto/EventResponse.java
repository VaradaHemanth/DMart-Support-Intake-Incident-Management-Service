package com.dmart.support_intake.dto;

public class EventResponse {

    private String status;
    private String jiraKey;
    private boolean duplicate;
    private int occurrenceCount;

    public EventResponse(String status, String jiraKey, boolean duplicate, int occurrenceCount) {
        this.status = status;
        this.jiraKey = jiraKey;
        this.duplicate = duplicate;
        this.occurrenceCount = occurrenceCount;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getJiraKey() { return jiraKey; }
    public void setJiraKey(String jiraKey) { this.jiraKey = jiraKey; }

    public boolean isDuplicate() { return duplicate; }
    public void setDuplicate(boolean duplicate) { this.duplicate = duplicate; }

    public int getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(int occurrenceCount) { this.occurrenceCount = occurrenceCount; }
}