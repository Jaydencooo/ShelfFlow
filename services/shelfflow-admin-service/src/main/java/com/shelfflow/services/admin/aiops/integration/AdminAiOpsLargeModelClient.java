package com.shelfflow.services.admin.aiops.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shelfflow.services.admin.aiops.config.AdminAiOpsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AdminAiOpsLargeModelClient {

    private static final Logger log = LoggerFactory.getLogger(AdminAiOpsLargeModelClient.class);
    private static final int HTTP_SUCCESS_MIN = 200;
    private static final int HTTP_SUCCESS_MAX_EXCLUSIVE = 300;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AdminAiOpsProperties properties;
    private final ObjectMapper objectMapper;

    public AdminAiOpsLargeModelClient(AdminAiOpsProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public Optional<String> chat(String systemPrompt, String userPrompt) {
        if (!properties.isExternalModelConfigured()) {
            return Optional.empty();
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(resolveChatCompletionsUri())
                    .timeout(Duration.ofSeconds(properties.getRequestTimeoutSeconds()))
                    .header(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE)
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(systemPrompt, userPrompt)))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(properties.getRequestTimeoutSeconds()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < HTTP_SUCCESS_MIN || response.statusCode() >= HTTP_SUCCESS_MAX_EXCLUSIVE) {
                log.warn("AI ops large model request failed, provider={}, model={}, status={}",
                        properties.getProvider(), properties.getModel(), response.statusCode());
                return Optional.empty();
            }

            return extractAnswer(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("AI ops large model request interrupted, provider={}, model={}", properties.getProvider(), properties.getModel(), exception);
            return Optional.empty();
        } catch (IOException | IllegalArgumentException exception) {
            log.warn("AI ops large model request unavailable, provider={}, model={}", properties.getProvider(), properties.getModel(), exception);
            return Optional.empty();
        }
    }

    private String buildRequestBody(String systemPrompt, String userPrompt) throws IOException {
        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "temperature", properties.getTemperature(),
                "max_tokens", properties.getMaxTokens(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );
        return objectMapper.writeValueAsString(body);
    }

    private Optional<String> extractAnswer(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (!content.isTextual() || content.asText().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(content.asText().trim());
    }

    private URI resolveChatCompletionsUri() {
        String baseUrl = trimTrailingSlash(properties.getBaseUrl());
        String path = properties.getChatCompletionsPath();
        String normalizedPath = path == null || path.isBlank() ? "/v1/chat/completions" : path.trim();
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        return URI.create(baseUrl + normalizedPath);
    }

    private String trimTrailingSlash(String value) {
        String normalized = value == null ? "" : value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
