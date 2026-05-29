package com.shelfflow.services.gateway.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "shelfflow.gateway.sentinel")
public class GatewaySentinelRuleProperties {

    private boolean enabled;
    private List<RouteRuleDefinition> routeRules = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<RouteRuleDefinition> getRouteRules() {
        return routeRules;
    }

    public void setRouteRules(List<RouteRuleDefinition> routeRules) {
        this.routeRules = routeRules == null ? new ArrayList<>() : routeRules;
    }

    public static class RouteRuleDefinition {
        private String routeId;
        private long intervalSeconds;
        private double count;

        public String getRouteId() {
            return routeId;
        }

        public void setRouteId(String routeId) {
            this.routeId = routeId;
        }

        public long getIntervalSeconds() {
            return intervalSeconds;
        }

        public void setIntervalSeconds(long intervalSeconds) {
            this.intervalSeconds = intervalSeconds;
        }

        public double getCount() {
            return count;
        }

        public void setCount(double count) {
            this.count = count;
        }
    }
}
