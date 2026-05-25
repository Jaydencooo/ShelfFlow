package com.shelfflow.services.admin.aiops.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shelfflow.admin.ai-ops")
public class AdminAiOpsProperties {

    private String provider = "local";
    private String model = "shelfflow-rules-v1";
    private String apiKey = "";
    private String baseUrl = "";
    private String chatCompletionsPath = "/v1/chat/completions";
    private int requestTimeoutSeconds = 20;
    private int maxTokens = 1200;
    private double temperature = 0.2D;
    private int suggestionLimit = 20;
    private int retrievalLimit = 4;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getChatCompletionsPath() {
        return chatCompletionsPath;
    }

    public void setChatCompletionsPath(String chatCompletionsPath) {
        this.chatCompletionsPath = chatCompletionsPath;
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public boolean isExternalModelConfigured() {
        return apiKey != null && !apiKey.isBlank() && baseUrl != null && !baseUrl.isBlank() && provider != null && !"local".equalsIgnoreCase(provider);
    }

    public int getSuggestionLimit() {
        return suggestionLimit;
    }

    public void setSuggestionLimit(int suggestionLimit) {
        this.suggestionLimit = suggestionLimit;
    }

    public int getRetrievalLimit() {
        return retrievalLimit;
    }

    public void setRetrievalLimit(int retrievalLimit) {
        this.retrievalLimit = retrievalLimit;
    }
}
