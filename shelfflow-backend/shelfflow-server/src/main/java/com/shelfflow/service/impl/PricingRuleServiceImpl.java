package com.shelfflow.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.shelfflow.dto.PricingRuleDTO;
import com.shelfflow.dto.PricingRulePageQueryDTO;
import com.shelfflow.entity.PricingRule;
import com.shelfflow.entity.Product;
import com.shelfflow.mapper.InventoryBatchMapper;
import com.shelfflow.mapper.PricingRuleMapper;
import com.shelfflow.mapper.ProductMapper;
import com.shelfflow.result.PageResult;
import com.shelfflow.service.PricingRuleService;
import com.shelfflow.vo.InventoryBatchVO;
import com.shelfflow.vo.PricingRuleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PricingRuleServiceImpl implements PricingRuleService {

    @Autowired
    private PricingRuleMapper pricingRuleMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private InventoryBatchMapper inventoryBatchMapper;

    @Override
    public void add(PricingRuleDTO pricingRuleDTO) {
        PricingRule pricingRule = new PricingRule();
        BeanUtils.copyProperties(pricingRuleDTO, pricingRule);
        pricingRuleMapper.insert(pricingRule);
    }

    @Override
    public void update(PricingRuleDTO pricingRuleDTO) {
        PricingRule pricingRule = new PricingRule();
        BeanUtils.copyProperties(pricingRuleDTO, pricingRule);
        pricingRuleMapper.update(pricingRule);
    }

    @Override
    public PageResult pageQuery(PricingRulePageQueryDTO pricingRulePageQueryDTO) {
        PageHelper.startPage(pricingRulePageQueryDTO.getPage(), pricingRulePageQueryDTO.getPageSize());
        Page<PricingRuleVO> page = pricingRuleMapper.pageQuery(pricingRulePageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public PricingRuleVO getById(Long id) {
        PricingRule pricingRule = pricingRuleMapper.getById(id);
        PricingRuleVO pricingRuleVO = new PricingRuleVO();
        BeanUtils.copyProperties(pricingRule, pricingRuleVO);
        return pricingRuleVO;
    }

    @Override
    public void setStatusById(Integer status, Long id) {
        PricingRule pricingRule = PricingRule.builder().id(id).status(status).build();
        pricingRuleMapper.update(pricingRule);
    }

    @Override
    public BigDecimal calculateDynamicPrice(Long productId, Long batchId) {
        Product product = productMapper.getById(productId);
        InventoryBatchVO batch = inventoryBatchMapper.getById(batchId);
        if (product == null || batch == null || product.getPrice() == null || batch.getExpirationTime() == null) {
            return BigDecimal.ZERO;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpirationTime().toLocalDate());
        if (days < 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discountRate = BigDecimal.ONE;
        List<PricingRule> rules = pricingRuleMapper.listEnabled();
        for (PricingRule rule : rules) {
            if (rule.getMinDaysToExpire() != null && rule.getMaxDaysToExpire() != null
                    && days >= rule.getMinDaysToExpire() && days <= rule.getMaxDaysToExpire()) {
                discountRate = rule.getDiscountRate();
                break;
            }
        }
        return product.getPrice().multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }
}
