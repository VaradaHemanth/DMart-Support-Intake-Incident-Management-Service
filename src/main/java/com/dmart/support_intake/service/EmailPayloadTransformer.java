package com.dmart.support_intake.service;

import com.dmart.support_intake.dto.EventRequest;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailPayloadTransformer {

    private static final Logger log = LoggerFactory.getLogger(EmailPayloadTransformer.class);

    // Key-value pair extraction regex (strict token matching to prevent trailing key/value leaks)
    private static final Pattern KEY_VALUE_PATTERN = 
        Pattern.compile("(?i)(IntegrationName|Node|ErrorCode|Env|OrderNo)\\s*[:=]\\s*([A-Za-z0-9_-]+)");

    // Regex to extract clean Order Number format (e.g., ORD-2026-44109)
    private static final Pattern STRICT_ORDER_NO_PATTERN = 
        Pattern.compile("(?i)\\b(ORD-[A-Za-z0-9-]+)\\b");

    // Regex to extract text under "Details:" up until divider lines or signatures
    private static final Pattern DETAILS_PATTERN = 
        Pattern.compile("(?i)Details:\\s*(.+?)(?=\r?\n-{2,}|\r?\nRegards|\r?\nThanks|\r?\nBest|\r?\nUnsubscribe|$)", Pattern.DOTALL);

    // DMart system keywords for valid incident filtering
    private static final List<String> DMART_SYSTEM_KEYWORDS = Arrays.asList(
        "DMART", "STERLING", "OMS", "WMS", "MQ_CONNECTION_ERROR", "OMS_MQ", "DMART_SUPPORT"
    );

    public EventRequest transform(Message message) {
        try {
            String subject = message.getSubject() != null ? message.getSubject() : "No Subject";
            String rawBody = extractBodyText(message);

            Address[] senders = message.getFrom();
            String senderAddress = (senders != null && senders.length > 0) ? senders[0].toString() : "EMAIL_UNKNOWN";

            Map<String, String> extractedMap = parseKeyValuePairs(rawBody);

            // Strict DMart Identifier Check
            if (!isDmartSupportIncident(subject, rawBody, extractedMap)) {
                log.debug("Email does not contain DMart identifiers. Skipping subject: '{}'", subject);
                return null;
            }

            // Extract ONLY clean description text
            String cleanDescription = extractCleanDescription(rawBody);

            // Extract clean Order Number (strips attached signatures)
            String cleanOrderNo = extractCleanOrderNo(extractedMap.get("orderno"), rawBody);

            EventRequest request = new EventRequest();
            request.setSourceChannel("EMAIL_INBOX");
            request.setSourceId(senderAddress);
            request.setSubject(cleanSubject(subject));
            request.setBodyText(cleanDescription);

            request.setIntegrationName(extractedMap.getOrDefault("integrationname", extractFallbackFromSubject(subject, "DMART_DEFAULT_INTEGRATION")));
            request.setNode(extractedMap.getOrDefault("node", "UNKNOWN_NODE"));
            request.setErrorCode(extractedMap.getOrDefault("errorcode", "ERR_GENERIC_DMART"));
            request.setEnv(extractedMap.getOrDefault("env", "PROD"));
            request.setOrderNo(cleanOrderNo);

            return request;

        } catch (Exception e) {
            log.error("Failed to transform DMart email payload", e);
            return null;
        }
    }

    /**
     * Checks if the email strictly matches DMart incident patterns.
     */
    private boolean isDmartSupportIncident(String subject, String body, Map<String, String> extractedMap) {
        String upperSubject = subject.toUpperCase();
        String upperBody = body.toUpperCase();

        // 1. Check if an extracted parameter matches a DMart system
        if (extractedMap.containsKey("integrationname")) {
            String integration = extractedMap.get("integrationname").toUpperCase();
            for (String keyword : DMART_SYSTEM_KEYWORDS) {
                if (integration.contains(keyword)) {
                    return true;
                }
            }
        }

        // 2. Check if OrderNo uses DMart format (e.g., ORD-)
        if (extractedMap.containsKey("orderno") && extractedMap.get("orderno").toUpperCase().startsWith("ORD-")) {
            return true;
        }

        // 3. Check if Subject or Body contains any explicit DMart identifier
        for (String keyword : DMART_SYSTEM_KEYWORDS) {
            if (upperSubject.contains(keyword) || upperBody.contains("INTEGRATIONNAME: " + keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Cleans the email body to retain ONLY the main problem description.
     */
    private String extractCleanDescription(String body) {
        if (body == null || body.trim().isEmpty()) {
            return "No details provided.";
        }

        String targetText = body;

        // Extract content under "Details:" if present
        Matcher matcher = DETAILS_PATTERN.matcher(body);
        if (matcher.find()) {
            targetText = matcher.group(1);
        }

        // Strip HTML tags, key-value lines, divider lines, and signatures
        String cleaned = targetText.replaceAll("<[^>]*>", "")
                                   .replaceAll("(?s)<style.*?>.*?</style>", "")
                                   .replaceAll("(?i)(IntegrationName|Node|ErrorCode|Env|OrderNo)\\s*[:=]\\s*([A-Za-z0-9_-]+)", "")
                                   .replaceAll("-{2,}.*", "")                            // Strips '-----' and everything after
                                   .replaceAll("(?i)(Regards|Thanks|Best|Cheers).*", "")    // Strips 'Regards Hemanth...'
                                   .replaceAll("\\b\\d{10}\\b", "")                       // Strips 10-digit phone numbers
                                   .replaceAll("(?m)^[ \\t]*\\r?\\n", "");                // Strips empty lines

        cleaned = cleaned.trim().replaceAll("\n{3,}", "\n\n");
        return cleaned.isEmpty() ? "No details provided." : cleaned;
    }

    /**
     * Ensures OrderNo contains strictly the order ID format without attached text.
     */
    private String extractCleanOrderNo(String parsedOrderNo, String body) {
        String valueToTest = parsedOrderNo != null ? parsedOrderNo : body;
        Matcher matcher = STRICT_ORDER_NO_PATTERN.matcher(valueToTest);
        if (matcher.find()) {
            return matcher.group(1); // Returns purely "ORD-2026-44109"
        }
        return "N/A";
    }

    private String cleanSubject(String subject) {
        return subject.replaceAll("(?i)^Fwd:\\s*", "")
                      .replaceAll("(?i)^Re:\\s*", "")
                      .trim();
    }

    private String extractBodyText(Part part) throws Exception {
        if (part.isMimeType("text/plain")) {
            return part.getContent().toString();
        }
        if (part.isMimeType("text/html")) {
            return part.getContent().toString().replaceAll("<[^>]*>", "");
        }
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                sb.append(extractBodyText(multipart.getBodyPart(i)));
            }
            return sb.toString();
        }
        return "";
    }

    private Map<String, String> parseKeyValuePairs(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) {
            return map;
        }

        Matcher matcher = KEY_VALUE_PATTERN.matcher(body);
        while (matcher.find()) {
            String key = matcher.group(1).trim().toLowerCase();
            String value = matcher.group(2).trim();
            map.put(key, value);
        }
        return map;
    }

    private String extractFallbackFromSubject(String subject, String defaultValue) {
        String upper = subject.toUpperCase();
        if (upper.contains("MQ")) {
            return "MQ_CONNECTION_ERROR";
        } else if (upper.contains("STERLING") || upper.contains("OMS")) {
            return "STERLING_OMS";
        }
        return defaultValue;
    }
}