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
}
