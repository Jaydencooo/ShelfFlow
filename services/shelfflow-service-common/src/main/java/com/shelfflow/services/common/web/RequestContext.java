package com.shelfflow.services.common.web;

public final class RequestContext {
    public static final String REQUEST_ID_ATTRIBUTE = "shelfflow.requestId";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String ADMIN_USER_ID_ATTRIBUTE = "shelfflow.adminUserId";
    public static final String ADMIN_ACCESS_TOKEN_ATTRIBUTE = "shelfflow.adminAccessToken";
    public static final String USER_ID_ATTRIBUTE = "shelfflow.userId";
    public static final String USER_OPEN_ID_ATTRIBUTE = "shelfflow.userOpenId";
    public static final String USER_ACCESS_TOKEN_ATTRIBUTE = "shelfflow.userAccessToken";

    private RequestContext() {
    }
}
