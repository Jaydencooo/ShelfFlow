package com.shelfflow.service.impl;

import com.shelfflow.constant.StatusConstant;
import com.shelfflow.entity.Orders;
import com.shelfflow.entity.Bundle;
import com.shelfflow.mapper.*;
import com.shelfflow.service.WorkspaceService;
import com.shelfflow.vo.BusinessDataVO;
import com.shelfflow.vo.ProductOverviewVO;
import com.shelfflow.vo.OrderOverViewVO;
import com.shelfflow.vo.BundleOverviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private BundleMapper bundleMapper;


    @Override
    public BusinessDataVO businessData(LocalDateTime beginTime, LocalDateTime endTime) {
        Double flowAmount = 0.0;//流转额
        Integer validOrderCount = 0;//有效订单数
        Integer allOrderCount = 0; //所有订单数（为了计算完成率的，不用传给前端）
        Double orderCompletionRate = 0.0;//订单完成率
        Double unitPrice = 0.0;//平均订单金额
        Integer newUsers = 0;//新增用户数

        Map map = new HashMap();
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        allOrderCount = ordersMapper.getOrdersByMap(map);
        map.put("status", Orders.COMPLETED);

        validOrderCount = ordersMapper.getOrdersByMap(map);
        if(allOrderCount != null && allOrderCount != 0 && validOrderCount != null){
            orderCompletionRate = validOrderCount * 1.0 / allOrderCount;
        }
        flowAmount = ordersMapper.getFlowAmountByMap(map);

        newUsers = userMapper.getUserByMap(map);

        if(validOrderCount != null && validOrderCount != 0 && flowAmount != null){
            unitPrice = flowAmount/ validOrderCount;
        }

        //前端展示格式统一
        if(flowAmount == null){
            flowAmount = 0.0;
        }

        return BusinessDataVO.builder()
                .validOrderCount(validOrderCount)
                .newUsers(newUsers)
                .orderCompletionRate(orderCompletionRate)
                .flowAmount(flowAmount)
                .unitPrice(unitPrice)
                .build();

    }

    @Override
    public OrderOverViewVO overviewOrders() {
        //待接单数量
        Integer waitingOrders = 0;
        //备货中数量
        Integer deliveredOrders = 0;
        //已完成数量
        Integer completedOrders = 0;
        //已取消数量
        Integer cancelledOrders = 0;
        //全部订单
        Integer allOrders = 0;

        Map map = new HashMap();
        map.put("beginTime", LocalDateTime.now().with(LocalTime.MIN));
        allOrders = ordersMapper.getOrdersByMap(map);

        map.put("status", Orders.TO_BE_CONFIRMED);
        waitingOrders = ordersMapper.getOrdersByMap(map);
        map.put("status", Orders.CONFIRMED);
        deliveredOrders = ordersMapper.getOrdersByMap(map);
        map.put("status", Orders.COMPLETED);
        completedOrders = ordersMapper.getOrdersByMap(map);
        map.put("status", Orders.CANCELLED);
        cancelledOrders = ordersMapper.getOrdersByMap(map);

        return OrderOverViewVO.builder()
                .allOrders(allOrders)
                .completedOrders(completedOrders)
                .deliveredOrders(deliveredOrders)
                .waitingOrders(waitingOrders)
                .cancelledOrders(cancelledOrders)
                .build();
    }

    @Override
    public ProductOverviewVO overviewProducts() {
        // 已启售数量
        Integer sold = 0;
        // 已停售数量
        Integer discontinued = 0;

        sold = productMapper.countByStatus(StatusConstant.ENABLE);
        discontinued = productMapper.countByStatus(StatusConstant.DISABLE);

        return new ProductOverviewVO(sold, discontinued);
    }

    @Override
    public BundleOverviewVO overviewBundles() {
        // 已启售数量
        Integer sold = 0;
        // 已停售数量
        Integer discontinued = 0;

        sold = bundleMapper.countByStatus(StatusConstant.ENABLE);
        discontinued = bundleMapper.countByStatus(StatusConstant.DISABLE);

        return new BundleOverviewVO(sold, discontinued);
    }
}
