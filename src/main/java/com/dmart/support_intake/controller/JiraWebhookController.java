package com.dmart.support_intake.controller;

import com.dmart.support_intake.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/jira")
public class JiraWebhookController {

    private final EventService eventService;

    public JiraWebhookController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<String> handleJiraWebhook(@RequestBody Map<String, Object> payload) {
        if (payload.containsKey("issue")) {
            Map<String, Object> issue = (Map<String, Object>) payload.get("issue");
            String jiraKey = (String) issue.get("key");

            Map<String, Object> fields = (Map<String, Object>) issue.get("fields");
            if (fields != null && fields.containsKey("status")) {
                Map<String, Object> statusObj = (Map<String, Object>) fields.get("status");
                String statusName = (String) statusObj.get("name");

                boolean updated = eventService.updateStatusFromJira(jiraKey, statusName);
                if (updated) {
                    return ResponseEntity.ok("Database status updated to: " + statusName);
                }
            }
        }
        return ResponseEntity.ok("Webhook received.");
    }
}