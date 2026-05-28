package com.shelfflow.services.admin.operationlog.web;

import com.shelfflow.services.admin.operationlog.service.AdminOperationLogApplicationService;
import com.shelfflow.services.common.web.RequestContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

public class AdminOperationLogInterceptor implements HandlerInterceptor {

    private static final String MODULE_PRODUCTS = "商品管理";
    private static final String MODULE_BATCHES = "批次管理";
    private static final String MODULE_PRICING = "定价规则";
    private static final String MODULE_ORDERS = "订单履约";
    private static final String MODULE_PICKUP_POINTS = "自提点管理";
    private static final String MODULE_AI_OPS = "AI 运营助手";
    private static final String MODULE_LOSS_STATS = "经营分析";
    private static final String MODULE_ADMIN = "管理端";
    private static final String ACTION_CREATE_OR_EXECUTE = "新增或执行";
    private static final String ACTION_UPDATE = "更新";
    private static final String ACTION_DELETE = "删除";
    private static final String ACTION_STATUS_CHANGE = "状态变更";
    private static final String ACTION_PICKUP_VERIFY = "自提核销";
    private static final String ACTION_OPERATE = "操作";

    private final AdminOperationLogApplicationService operationLogApplicationService;

    public AdminOperationLogInterceptor(AdminOperationLogApplicationService operationLogApplicationService) {
        this.operationLogApplicationService = operationLogApplicationService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        if (!shouldRecord(request)) {
            return;
        }
        Object actorId = request.getAttribute(RequestContext.ADMIN_USER_ID_ATTRIBUTE);
        operationLogApplicationService.record(
                actorId instanceof Long ? (Long) actorId : null,
                resolveModule(request.getRequestURI()),
                resolveAction(request.getMethod(), request.getRequestURI()),
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                request.getHeader(RequestContext.REQUEST_ID_HEADER)
        );
    }

    private boolean shouldRecord(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase(Locale.ROOT);
        return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
    }

    private String resolveModule(String path) {
        if (path.contains("/products")) {
            return MODULE_PRODUCTS;
        }
        if (path.contains("/inventory-batches")) {
            return MODULE_BATCHES;
        }
        if (path.contains("/pricing-rules")) {
            return MODULE_PRICING;
        }
        if (path.contains("/orders")) {
            return MODULE_ORDERS;
        }
        if (path.contains("/pickup-points")) {
            return MODULE_PICKUP_POINTS;
        }
        if (path.contains("/ai-ops")) {
            return MODULE_AI_OPS;
        }
        if (path.contains("/loss-stats")) {
            return MODULE_LOSS_STATS;
        }
        return MODULE_ADMIN;
    }

    private String resolveAction(String method, String path) {
        if (path.contains("/pickup-verification")) {
            return ACTION_PICKUP_VERIFY;
        }
        if (path.contains("/status")) {
            return ACTION_STATUS_CHANGE;
        }
        return switch (method.toUpperCase(Locale.ROOT)) {
            case "POST" -> ACTION_CREATE_OR_EXECUTE;
            case "PUT" -> ACTION_UPDATE;
            case "DELETE" -> ACTION_DELETE;
            default -> ACTION_OPERATE;
        };
    }
}
