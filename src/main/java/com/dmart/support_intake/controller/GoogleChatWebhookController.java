package com.dmart.support_intake.controller;

import com.dmart.support_intake.dto.EventRequest;
import com.dmart.support_intake.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/webhooks/gchat")
public class GoogleChatWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GoogleChatWebhookController.class);
    
    private final EventService eventService;

    public GoogleChatWebhookController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<String> handleGoogleChatWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // Support both direct clean JSON format AND nested Google Chat format
            String text = "";
            String senderName = "GCHAT_USER";

            if (payload.containsKey("message") && payload.get("message") instanceof Map) {
                // Handling nested Google Chat format if it comes in
                Map<String, Object> message = (Map<String, Object>) payload.get("message");
                text = (String) message.getOrDefault("text", "");
                if (message.containsKey("sender") && message.get("sender") instanceof Map) {
                    Map<String, Object> sender = (Map<String, Object>) message.get("sender");
                    senderName = (String) sender.getOrDefault("displayName", "GCHAT_USER");
                }
            } else {
                // Handling normal/flat JSON payload (e.g., {"text": "...", "senderName": "..."})
                text = (String) payload.getOrDefault("text", "");
                senderName = (String) payload.getOrDefault("senderName", "GCHAT_USER");
            }

            if (text != null && !text.trim().isEmpty()) {
                EventRequest request = parseChatToEventRequest(text, senderName);

                if (request != null) {
                    eventService.processEvent(request);
                    log.info("Successfully received and processed event locally from: {}", senderName);
                }
            }

            return ResponseEntity.ok("Event acknowledged.");
        } catch (Exception e) {
            log.error("Failed to process webhook payload", e);
            return ResponseEntity.badRequest().body("Error processing payload.");
        }
    }

    private EventRequest parseChatToEventRequest(String text, String senderName) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        EventRequest request = new EventRequest();
        request.setSourceChannel("GOOGLE_CHAT");
        request.setSourceId(senderName);
        request.setSubject("Chat Alert: " + (text.length() > 30 ? text.substring(0, 30) + "..." : text));
        request.setBodyText(text);

        // Robust regex parameter matching
        request.setIntegrationName(extractParam(text, "IntegrationName", "DMART_DEFAULT_INTEGRATION"));
        request.setNode(extractParam(text, "Node", "GCHAT_NODE"));
        request.setErrorCode(extractParam(text, "ErrorCode", "ERR_GCHAT_ALERT"));
        request.setEnv(extractParam(text, "Env", "PROD"));
        request.setOrderNo(extractParam(text, "OrderNo", "ORD-2026-LOCAL"));

        return request;
    }

    private String extractParam(String text, String key, String defaultValue) {
        try {
            Pattern pattern = Pattern.compile("(?i)\\b" + key + "\\s*[:=]\\s*([A-Za-z0-9_-]+)");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String val = matcher.group(1).trim();
                if (!val.isEmpty()) {
                    return val;
                }
            }
        } catch (Exception ignored) {}
        return defaultValue;
    }
}