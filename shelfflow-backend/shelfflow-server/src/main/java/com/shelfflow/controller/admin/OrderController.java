package com.shelfflow.controller.admin;

import com.shelfflow.dto.*;
import com.shelfflow.result.PageResult;
import com.shelfflow.result.Result;
import com.shelfflow.service.OrderService;
import com.shelfflow.vo.OrderStatisticsVO;
import com.shelfflow.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "管理端-订单接口")
public class OrderController {
    @Autowired
    OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("订单分页查询")
    public Result<PageResult> ordersPageQuery(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单分页查询,ordersPageQueryDTO:{}",ordersPageQueryDTO);
        PageResult pageResult =orderService.ordersPageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    @ApiOperation("查询各状态订单数量")
    public Result<OrderStatisticsVO> getStatistics(){
        log.info("查询各状态订单数量");
        OrderStatisticsVO orderStatisticsVO = orderService.getStatistics();
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getDetail(@PathVariable Long id){
        log.info("查询订单详情,id:{}",id);
        OrderVO orderVO = orderService.getById(id);
        return Result.success(orderVO);
    }

    @PutMapping("/confirm")
    @ApiOperation("接单(status修改为已接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("status修改为已接单,ordersConfirmDTO:{}",ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception{
        log.info("拒单,ordersRejectionDTO:{}",ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception{
        log.info("取消订单, ordersCancelDTO:{}",ordersCancelDTO);
        orderService.cancelByAdmin(ordersCancelDTO);
        return Result.success();
    }


    @PutMapping("/ready-for-pickup/{id}")
    @ApiOperation("备货完成，订单进入待自提")
    public Result readyForPickup(@PathVariable Long id){
        log.info("备货完成，订单进入待自提, id:{}",id);
        orderService.readyForPickup(id);
        return Result.success();
    }

    @PutMapping("/verify")
    @ApiOperation("核销自提订单")
    public Result verify(@RequestBody OrdersVerifyDTO ordersVerifyDTO){
        log.info("核销自提订单, ordersVerifyDTO:{}",ordersVerifyDTO);
        orderService.verify(ordersVerifyDTO);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单(修改status为已完成")
    public Result complete(@PathVariable Long id){
        log.info("完成订单(修改status为已完成), id:{}",id);
        orderService.complete(id);
        return Result.success();
    }

}
