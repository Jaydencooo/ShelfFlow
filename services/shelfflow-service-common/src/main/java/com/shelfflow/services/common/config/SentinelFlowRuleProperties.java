package com.shelfflow.services.common.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "shelfflow.sentinel")
public class SentinelFlowRuleProperties {

    public static final String GRADE_QPS = "qps";
    public static final String GRADE_THREAD = "thread";

    private boolean enabled;
    private List<FlowRuleDefinition> flowRules = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<FlowRuleDefinition> getFlowRules() {
        return flowRules;
    }

    public void setFlowRules(List<FlowRuleDefinition> flowRules) {
        this.flowRules = flowRules == null ? new ArrayList<>() : flowRules;
    }

    public static class FlowRuleDefinition {
        private String resource;
        private String grade = GRADE_QPS;
        private double count;

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public double getCount() {
            return count;
        }

        public void setCount(double count) {
            this.count = count;
        }
    }
}
