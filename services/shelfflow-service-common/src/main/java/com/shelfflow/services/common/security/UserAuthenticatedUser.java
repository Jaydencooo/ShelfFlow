package com.shelfflow.services.common.security;

public class UserAuthenticatedUser {

    private final Long userId;
    private final String openId;
    private final String accessToken;

    public UserAuthenticatedUser(Long userId, String openId, String accessToken) {
        this.userId = userId;
        this.openId = openId;
        this.accessToken = accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public String getOpenId() {
        return openId;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
