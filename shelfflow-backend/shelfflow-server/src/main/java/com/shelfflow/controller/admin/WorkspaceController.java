package com.shelfflow.controller.admin;

import com.shelfflow.result.Result;
import com.shelfflow.service.WorkspaceService;
import com.shelfflow.vo.BusinessDataVO;
import com.shelfflow.vo.ProductOverviewVO;
import com.shelfflow.vo.OrderOverViewVO;
import com.shelfflow.vo.BundleOverviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/admin/workspace")
@Slf4j
@Api(tags = "工作台接口")
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/businessData")
    @ApiOperation("今日数据汇总统计")
    public Result<BusinessDataVO> businessData(){
        log.info("今日数据汇总统计");
        LocalDateTime beginOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX); //其实不用写这一行
        return Result.success(workspaceService.businessData(beginOfDay, endOfDay));
    }

    @GetMapping("/overviewOrders")
    @ApiOperation("订单总览")
    public Result<OrderOverViewVO> overviewOrders(){
        log.info("订单总览");
        return Result.success(workspaceService.overviewOrders());
    }

    @GetMapping("/overviewProducts")
    @ApiOperation("商品总览")
    public Result<ProductOverviewVO> overviewProducts(){
        log.info("商品总览");
        return Result.success(workspaceService.overviewProducts());
    }

    @GetMapping("/overviewBundles")
    @ApiOperation("商品总览")
    public Result<BundleOverviewVO> overviewBundles(){
        log.info("商品总览");
        return Result.success(workspaceService.overviewBundles());
    }


}
