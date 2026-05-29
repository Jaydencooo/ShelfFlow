package com.shelfflow.services.common.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "shelfflow.sentinel", name = "enabled", havingValue = "true")
public class SentinelFlowRuleConfiguration implements ApplicationRunner {

    private final SentinelFlowRuleProperties properties;

    public SentinelFlowRuleConfiguration(SentinelFlowRuleProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<FlowRule> rules = properties.getFlowRules().stream()
                .map(this::toFlowRule)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
        if (rules.isEmpty()) {
            log.info("Sentinel flow protection enabled without local flow rules");
            return;
        }
        FlowRuleManager.loadRules(rules);
        log.info("Loaded {} Sentinel flow rules", rules.size());
    }

    private FlowRule toFlowRule(SentinelFlowRuleProperties.FlowRuleDefinition definition) {
        if (definition.getResource() == null || definition.getResource().isBlank() || definition.getCount() <= 0) {
            log.warn("Skip invalid Sentinel flow rule: resource={}, count={}",
                    definition.getResource(),
                    definition.getCount());
            return null;
        }
        FlowRule rule = new FlowRule();
        rule.setResource(definition.getResource().trim());
        rule.setGrade(resolveGrade(definition.getGrade()));
        rule.setCount(definition.getCount());
        return rule;
    }

    private int resolveGrade(String grade) {
        String normalizedGrade = grade == null ? SentinelFlowRuleProperties.GRADE_QPS : grade.toLowerCase(Locale.ROOT);
        if (SentinelFlowRuleProperties.GRADE_THREAD.equals(normalizedGrade)) {
            return RuleConstant.FLOW_GRADE_THREAD;
        }
        return RuleConstant.FLOW_GRADE_QPS;
    }
}
