package com.shelfflow.mapper;

import com.github.pagehelper.Page;
import com.shelfflow.anno.AutoFill;
import com.shelfflow.dto.PricingRulePageQueryDTO;
import com.shelfflow.entity.PricingRule;
import com.shelfflow.enumeration.OperationType;
import com.shelfflow.vo.PricingRuleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PricingRuleMapper {

    @AutoFill(OperationType.INSERT)
    void insert(PricingRule pricingRule);

    @AutoFill(OperationType.UPDATE)
    void update(PricingRule pricingRule);

    Page<PricingRuleVO> pageQuery(PricingRulePageQueryDTO pricingRulePageQueryDTO);

    @Select("select * from pricing_rule where id = #{id}")
    PricingRule getById(Long id);

    @Select("select * from pricing_rule where status = 1 order by priority desc, max_days_to_expire asc")
    List<PricingRule> listEnabled();
}
