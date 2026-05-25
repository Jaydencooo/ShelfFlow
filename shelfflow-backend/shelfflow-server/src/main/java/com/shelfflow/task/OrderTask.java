package com.shelfflow.task;


import com.shelfflow.constant.MessageConstant;
import com.shelfflow.entity.OrderDetail;
import com.shelfflow.entity.Orders;
import com.shelfflow.mapper.InventoryBatchMapper;
import com.shelfflow.mapper.OrderDetailMapper;
import com.shelfflow.mapper.OrdersMapper;
import com.shelfflow.service.FulfillmentTaskService;
import com.shelfflow.service.InventoryBatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private InventoryBatchMapper inventoryBatchMapper;
    @Autowired
    private FulfillmentTaskService fulfillmentTaskService;
    @Autowired
    private InventoryBatchService inventoryBatchService;

    @Scheduled(cron = "0 * * * * *")
    public void processTimeOutOrder(){
        log.info("处理超时未付款订单：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> list = ordersMapper.selectByStatusAndOrderTimeLt(Orders.PENDING_PAYMENT, time);
        if(list != null && !list.isEmpty()){//存在超时未支付订单
            list.forEach(orders -> {
                releaseLockedStock(orders.getId());
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason(MessageConstant.CUSTOMER_PAY_TIME_OUT);
                ordersMapper.updateById(orders);
                fulfillmentTaskService.markCancelledByOrderId(orders.getId());
            });
        }
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void refreshInventoryBatchStatus(){
        log.info("刷新库存批次状态：{}", LocalDateTime.now());
        inventoryBatchService.refreshStatuses();
    }

    public void processPickupOrder(){
        log.info("待自提订单必须由运营人员核销，不再自动完成:{}", LocalDateTime.now());
    }




    private void releaseLockedStock(Long orderId) {
        List<OrderDetail> details = orderDetailMapper.getByOrderId(orderId);
        if (details == null || details.isEmpty()) {
            return;
        }
        details.forEach(detail -> {
            if (detail.getBatchId() == null) {
                return;
            }
            int updated = inventoryBatchMapper.releaseLockedStock(detail.getBatchId(), detail.getNumber());
            if (updated != 1) {
                log.warn("超时订单释放锁定库存失败, orderId={}, batchId={}, quantity={}", orderId, detail.getBatchId(), detail.getNumber());
            }
        });
    }
}
