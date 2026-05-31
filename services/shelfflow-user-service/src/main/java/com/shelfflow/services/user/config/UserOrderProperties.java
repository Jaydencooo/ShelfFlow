package com.shelfflow.services.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "shelfflow.user.order")
public class UserOrderProperties {

    private String pickupPoint = "滨江社区前置仓 A 区";
    private String orderNumberPrefix = "SFU";
    private int pickupCodeLength = 6;
    private int pickupDeadlineHours = 24;
    private int defaultPayMethod = 1;
    private int defaultPreparationMode = 1;
    private int defaultFulfillmentType = 2;
    private int defaultPackageStrategy = 1;
    private int defaultFulfillmentFee = 0;
    private int defaultPackageCount = 0;
    private InventoryReservation inventoryReservation = new InventoryReservation();
    private Events events = new Events();
    private TimeoutClose timeoutClose = new TimeoutClose();
    private PaymentCallback paymentCallback = new PaymentCallback();

    @Data
    public static class InventoryReservation {
        private InventoryReservationMode mode = InventoryReservationMode.DATABASE;
        private String redisKeyPrefix = "shelfflow:inventory:batch";
        private long reservationTtlSeconds = 900;
        private boolean failOpen;
    }

    public enum InventoryReservationMode {
        DATABASE,
        REDIS_LUA
    }

    @Data
    public static class Events {
        private boolean enabled;
        private String exchange = "shelfflow.order.events";
        private String routingKeyPrefix = "shelfflow.order";
        private boolean durableExchange = true;
        private boolean failFast;
        private boolean outboxEnabled;
        private long outboxInitialDelayMilliseconds = 60000;
        private long outboxFixedDelayMilliseconds = 60000;
        private int outboxBatchSize = 100;
        private int outboxMaxAttempts = 5;
        private long outboxRetryDelaySeconds = 60;
    }

    @Data
    public static class TimeoutClose {
        private boolean enabled;
        private long unpaidTimeoutMinutes = 30;
        private long fixedDelayMilliseconds = 60000;
        private int batchSize = 100;
        private String cancelReason = "订单超时未支付，系统自动取消";
    }

    @Data
    public static class PaymentCallback {
        private boolean enabled;
        private boolean requireSignature = true;
        private String callbackSecret = "";
    }
}
