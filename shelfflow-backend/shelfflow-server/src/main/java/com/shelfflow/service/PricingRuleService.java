package com.shelfflow.service;

import com.shelfflow.dto.PricingRuleDTO;
import com.shelfflow.dto.PricingRulePageQueryDTO;
import com.shelfflow.result.PageResult;
import com.shelfflow.vo.PricingRuleVO;

import java.math.BigDecimal;

public interface PricingRuleService {

    void add(PricingRuleDTO pricingRuleDTO);

    void update(PricingRuleDTO pricingRuleDTO);

    PageResult pageQuery(PricingRulePageQueryDTO pricingRulePageQueryDTO);

    PricingRuleVO getById(Long id);

    void setStatusById(Integer status, Long id);

    BigDecimal calculateDynamicPrice(Long productId, Long batchId);
}
