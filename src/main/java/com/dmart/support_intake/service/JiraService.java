package com.dmart.support_intake.service;

import com.dmart.support_intake.dto.EventRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JiraService {

    private static final Logger log = LoggerFactory.getLogger(JiraService.class);

    @Value("${jira.base-url}")
    private String jiraUrl;

    @Value("${jira.email}")
    private String email;

    @Value("${jira.api-token}")
    private String apiToken;

    @Value("${jira.project-key:KAN}")
    private String projectKey;

    @Value("${jira.issue-type:Task}")
    private String issueType;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createJiraIssue(EventRequest request) {
        String url = jiraUrl + "/rest/api/3/issue";

        HttpHeaders headers = createAuthHeaders();
        Map<String, Object> payload = createJiraPayload(request);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                return response.getBody().get("key").toString();
            }
        } catch (HttpClientErrorException e) {
            log.error("Jira Request failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Jira issue creation failed.", e);
        }

        throw new RuntimeException("Failed to create Jira issue");
    }

    // Overloaded method in case you intended to pass an custom issue type or key directly
    public String createJiraIssue(EventRequest request, String customParam) {
        // Log or handle the custom String parameter as needed
        log.info("Processing request with parameter: {}", customParam);
        return createJiraIssue(request);
    }

    public void addComment(String jiraKey, String commentText) {
        String url = jiraUrl + "/rest/api/3/issue/" + jiraKey + "/comment";

        HttpHeaders headers = createAuthHeaders();

        Map<String, Object> bodyNode = Map.of(
            "type", "doc",
            "version", 1,
            "content", List.of(
                Map.of(
                    "type", "paragraph",
                    "content", List.of(
                        Map.of(
                            "type", "text",
                            "text", commentText
                        )
                    )
                )
            )
        );

        Map<String, Object> payload = Map.of("body", bodyNode);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(url, entity, Map.class);
            log.info("Added duplicate comment to Jira issue: {}", jiraKey);
        } catch (HttpClientErrorException e) {
            log.error("Failed to add comment to Jira issue {}: {}", jiraKey, e.getResponseBodyAsString());
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + getAuthHeader());
        return headers;
    }

    private String getAuthHeader() {
        String authStr = email + ":" + apiToken;
        return Base64.getEncoder().encodeToString(authStr.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, Object> createJiraPayload(EventRequest request) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("project", Map.of("key", projectKey.trim()));
        fields.put("summary", request.getSubject() != null ? request.getSubject() : "System Alert");
        fields.put("issuetype", Map.of("name", issueType));

        Map<String, Object> descriptionNode = Map.of(
            "type", "doc",
            "version", 1,
            "content", List.of(
                Map.of(
                    "type", "paragraph",
                    "content", List.of(
                        Map.of(
                            "type", "text",
                            "text", request.getBodyText() != null ? request.getBodyText() : "No details provided."
                        )
                    )
                )
            )
        );
        fields.put("description", descriptionNode);

        return Map.of("fields", fields);
    }
}