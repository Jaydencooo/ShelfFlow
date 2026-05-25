package com.shelfflow.controller.admin;

import com.shelfflow.dto.PricingRuleDTO;
import com.shelfflow.dto.PricingRulePageQueryDTO;
import com.shelfflow.result.PageResult;
import com.shelfflow.result.Result;
import com.shelfflow.service.PricingRuleService;
import com.shelfflow.vo.PricingRuleVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/admin/pricing-rule")
@Slf4j
public class PricingRuleController {

    @Autowired
    private PricingRuleService pricingRuleService;

    @PostMapping
    @ApiOperation("新增动态定价规则")
    public Result add(@RequestBody PricingRuleDTO pricingRuleDTO) {
        log.info("新增动态定价规则: {}", pricingRuleDTO);
        pricingRuleService.add(pricingRuleDTO);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改动态定价规则")
    public Result update(@RequestBody PricingRuleDTO pricingRuleDTO) {
        log.info("修改动态定价规则: {}", pricingRuleDTO);
        pricingRuleService.update(pricingRuleDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("动态定价规则分页查询")
    public Result<PageResult> pageQuery(PricingRulePageQueryDTO pricingRulePageQueryDTO) {
        return Result.success(pricingRuleService.pageQuery(pricingRulePageQueryDTO));
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询动态定价规则")
    public Result<PricingRuleVO> getById(@PathVariable Long id) {
        return Result.success(pricingRuleService.getById(id));
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用或停用动态定价规则")
    public Result setStatusById(@PathVariable Integer status, Long id) {
        pricingRuleService.setStatusById(status, id);
        return Result.success();
    }

    @GetMapping("/calculate")
    @ApiOperation("计算指定商品批次的动态价格")
    public Result<BigDecimal> calculateDynamicPrice(Long productId, Long batchId) {
        return Result.success(pricingRuleService.calculateDynamicPrice(productId, batchId));
    }
}
