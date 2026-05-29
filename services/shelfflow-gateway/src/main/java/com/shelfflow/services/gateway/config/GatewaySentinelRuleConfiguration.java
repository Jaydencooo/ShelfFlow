package com.shelfflow.services.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "shelfflow.gateway.sentinel", name = "enabled", havingValue = "true")
public class GatewaySentinelRuleConfiguration implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewaySentinelRuleConfiguration.class);

    private final GatewaySentinelRuleProperties properties;

    public GatewaySentinelRuleConfiguration(GatewaySentinelRuleProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        Set<GatewayFlowRule> rules = properties.getRouteRules().stream()
                .filter(this::isValidRule)
                .map(this::toGatewayFlowRule)
                .collect(Collectors.toUnmodifiableSet());
        if (rules.isEmpty()) {
            LOGGER.info("Gateway Sentinel protection enabled without local route rules");
            return;
        }
        GatewayRuleManager.loadRules(rules);
        LOGGER.info("Loaded {} Gateway Sentinel route rules", rules.size());
    }

    private boolean isValidRule(GatewaySentinelRuleProperties.RouteRuleDefinition definition) {
        boolean valid = definition.getRouteId() != null
                && !definition.getRouteId().isBlank()
                && definition.getIntervalSeconds() > 0
                && definition.getCount() > 0;
        if (!valid) {
            LOGGER.warn("Skip invalid Gateway Sentinel rule: routeId={}, intervalSeconds={}, count={}",
                    definition.getRouteId(),
                    definition.getIntervalSeconds(),
                    definition.getCount());
        }
        return valid;
    }

    private GatewayFlowRule toGatewayFlowRule(GatewaySentinelRuleProperties.RouteRuleDefinition definition) {
        return new GatewayFlowRule(definition.getRouteId().trim())
                .setIntervalSec(definition.getIntervalSeconds())
                .setCount(definition.getCount());
    }
}
