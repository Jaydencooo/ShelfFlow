package com.shelfflow.services.user.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "shelfflow.user.order.timeout-close", name = "enabled", havingValue = "true")
public class UserOrderTimeoutCloseScheduler {

    private final UserOrderTimeoutCloseService userOrderTimeoutCloseService;

    public UserOrderTimeoutCloseScheduler(UserOrderTimeoutCloseService userOrderTimeoutCloseService) {
        this.userOrderTimeoutCloseService = userOrderTimeoutCloseService;
    }

    @Scheduled(
            initialDelayString = "${shelfflow.user.order.timeout-close.fixed-delay-milliseconds:60000}",
            fixedDelayString = "${shelfflow.user.order.timeout-close.fixed-delay-milliseconds:60000}"
    )
    public void closeExpiredPendingPaymentOrders() {
        int closedCount = userOrderTimeoutCloseService.closeExpiredPendingPaymentOrders(LocalDateTime.now());
        if (closedCount > 0) {
            log.info("Timeout closed pending payment orders. count={}", closedCount);
        }
    }
}
