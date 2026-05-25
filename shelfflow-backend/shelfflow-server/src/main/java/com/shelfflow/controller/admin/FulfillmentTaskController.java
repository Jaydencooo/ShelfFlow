package com.shelfflow.controller.admin;

import com.shelfflow.dto.FulfillmentTaskPageQueryDTO;
import com.shelfflow.dto.OrdersCancelDTO;
import com.shelfflow.dto.OrdersVerifyDTO;
import com.shelfflow.result.PageResult;
import com.shelfflow.result.Result;
import com.shelfflow.service.FulfillmentTaskService;
import com.shelfflow.service.OrderService;
import com.shelfflow.vo.FulfillmentTaskVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/fulfillment-task")
@Slf4j
@Api(tags = "管理端-履约任务接口")
public class FulfillmentTaskController {

    @Autowired
    private FulfillmentTaskService fulfillmentTaskService;
    @Autowired
    private OrderService orderService;

    @GetMapping("/page")
    @ApiOperation("履约任务分页查询")
    public Result<PageResult> pageQuery(FulfillmentTaskPageQueryDTO fulfillmentTaskPageQueryDTO) {
        log.info("履约任务分页查询: {}", fulfillmentTaskPageQueryDTO);
        return Result.success(fulfillmentTaskService.pageQuery(fulfillmentTaskPageQueryDTO));
    }

    @GetMapping("/order/{orderId}")
    @ApiOperation("根据订单id查询履约任务")
    public Result<FulfillmentTaskVO> getByOrderId(@PathVariable Long orderId) {
        return Result.success(fulfillmentTaskService.getByOrderId(orderId));
    }

    @GetMapping("/statistics")
    @ApiOperation("履约任务状态统计")
    public Result<Map<String, Long>> statistics() {
        return Result.success(fulfillmentTaskService.statusStatistics());
    }

    @PutMapping("/ready-for-pickup/{orderId}")
    @ApiOperation("履约任务标记为待自提")
    public Result readyForPickup(@PathVariable Long orderId) {
        orderService.readyForPickup(orderId);
        return Result.success();
    }

    @PutMapping("/complete/{orderId}")
    @ApiOperation("履约任务核销完成")
    public Result complete(@PathVariable Long orderId) {
        orderService.complete(orderId);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消履约任务并取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        if (ordersCancelDTO.getCancelReason() == null || ordersCancelDTO.getCancelReason().trim().isEmpty()) {
            ordersCancelDTO.setCancelReason("履约任务取消");
        }
        orderService.cancelByAdmin(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/verify")
    @ApiOperation("履约任务按自提码核销")
    public Result verify(@RequestBody OrdersVerifyDTO ordersVerifyDTO) {
        orderService.verify(ordersVerifyDTO);
        return Result.success();
    }
}
