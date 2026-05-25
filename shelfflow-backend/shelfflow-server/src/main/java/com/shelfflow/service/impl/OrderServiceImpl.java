package com.shelfflow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.shelfflow.constant.MessageConstant;
import com.shelfflow.context.CurrentActorContext;
import com.shelfflow.dto.*;
import com.shelfflow.entity.*;
import com.shelfflow.exception.OrderBusinessException;
import com.shelfflow.mapper.*;
import com.shelfflow.result.PageResult;
import com.shelfflow.service.FulfillmentTaskService;
import com.shelfflow.service.OrderService;
import com.shelfflow.utils.WeChatPayUtil;
import com.shelfflow.vo.OrderPaymentVO;
import com.shelfflow.vo.OrderStatisticsVO;
import com.shelfflow.vo.OrderSubmitVO;
import com.shelfflow.vo.OrderVO;
import com.shelfflow.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private PickupContactMapper pickupContactMapper;
    @Autowired
    private CartItemMapper cartItemMapper;
    @Autowired
    private InventoryBatchMapper inventoryBatchMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private FulfillmentTaskService fulfillmentTaskService;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //1. 异常情况的处理（选品车为空）
        Long userId = CurrentActorContext.getCurrentId();
        PickupContact pickupContact = null;
        if (ordersSubmitDTO.getPickupContactId() != null) {
            pickupContact = pickupContactMapper.getById(ordersSubmitDTO.getPickupContactId());
        }
        User user = userMapper.getById(userId);
        //异常情况的处理:选品车为空
        CartItem cartItem = CartItem.builder()
                .userId(CurrentActorContext.getCurrentId())
                .build();
        List<CartItem> cartItems = cartItemMapper.list(cartItem);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        lockBatchStock(cartItems);

        //2. 添加orders表

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setUserId(userId);

        if (pickupContact != null) {
            orders.setPhone(pickupContact.getPhone());
            orders.setPickupContactId(pickupContact.getId());
            orders.setPickupPoint(pickupContact.getDetail());
            orders.setConsignee(pickupContact.getConsignee());
        } else if (user != null) {
            orders.setPhone(user.getPhone());
            orders.setConsignee(user.getName());
            orders.setPickupPoint("到店自提");
        }

        LocalDateTime pickupTime = ordersSubmitDTO.getPickupTime();
        LocalDateTime pickupDeadline = pickupTime != null ? pickupTime.plusHours(2) : LocalDateTime.now().plusHours(24);
        orders.setFulfillmentType(ordersSubmitDTO.getFulfillmentType() == null ? 2 : ordersSubmitDTO.getFulfillmentType());
        orders.setPickupCode(generatePickupCode());
        orders.setPickupTime(pickupTime);
        orders.setPickupDeadline(pickupDeadline);

        orders.setPreparationMode(ordersSubmitDTO.getPreparationMode());
        orders.setPackageCount(ordersSubmitDTO.getPackageCount() == null ? 0 : ordersSubmitDTO.getPackageCount());
        orders.setPackageStrategy(ordersSubmitDTO.getPackageStrategy());
        orders.setFulfillmentFee(ordersSubmitDTO.getFulfillmentFee() == null ? 0 : ordersSubmitDTO.getFulfillmentFee());
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));

        ordersMapper.insert(orders);

        //3. 添加orders——detail表(order和cartItem的关系）
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem s : cartItems) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .orderId(orders.getId())
                    .build();
            BeanUtils.copyProperties(s, orderDetail);
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);


        //4.删除shopping carts
        cartItemMapper.cleanByUserId(userId);
        //5. 返回数据

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .pickupCode(orders.getPickupCode())
                .pickupDeadline(orders.getPickupDeadline())
                .build();
        return orderSubmitVO;

    }

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        Long userId = CurrentActorContext.getCurrentId();
        User user = userMapper.getById(userId);
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(),
                new BigDecimal(0.01),
                "ShelfFlow支付",
                user.getOpenid()
        );
        if (jsonObject.getString("code") != null
                && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException(MessageConstant.ORDER_ALREADY_PAID);
        }
        OrderPaymentVO orderPaymentVO = jsonObject.toJavaObject(OrderPaymentVO.class);
        orderPaymentVO.setPackageStr(jsonObject.getString("package"));
        return orderPaymentVO;
    }

    @Override
    @Transactional
    public void paySuccess(String outTradeNo) {
        Orders orders = Orders.builder()
                .number(outTradeNo)
                .payStatus(Orders.PAID)
                .status(Orders.TO_BE_CONFIRMED)
                .checkoutTime(LocalDateTime.now())
                .build();
        ordersMapper.updateByNumberAndUserId(orders);
        Orders paidOrder = ordersMapper.getByNumber(outTradeNo);
        fulfillmentTaskService.createForOrder(paidOrder);

        //通过WebSocket，向客户端-管理端发送订单消息
        Map map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", paidOrder == null ? null : paidOrder.getId());
        map.put("content", "订单号" + outTradeNo);
        webSocketServer.sendToAllClient(JSON.toJSONString(map));

    }

    @Override
    public PageResult ordersPageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> pages = ordersMapper.ordersPageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();
        if (pages != null && pages.getPages() != 0) {
            for (Orders orders : pages.getResult()) {
                OrderVO orderVO = new OrderVO();
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
                orderVO.setOrderDetailList(orderDetails);
                BeanUtils.copyProperties(orders, orderVO);
                list.add(orderVO);
            }
        }
        return new PageResult(pages.getTotal(), list);

        //Page对象包含的关键信息：
        //getTotal()：总记录数
        //getResult()：当前页的数据列表

        //getPages()：总页数
        //getPageNum()：当前页码
        //getPageSize()：每页记录数
    }

    @Override
    public OrderVO getById(Long id) {
        Orders orders = ordersMapper.getById(id);
        List<OrderDetail> list = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(list);
        return orderVO;
    }

    @Override
    public void repetition(Long id) {
        Long userId = CurrentActorContext.getCurrentId();
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        List<CartItem> cartItemList = orderDetailList.stream()
                .map(o -> {
                    CartItem cartItem = new CartItem();
                    BeanUtils.copyProperties(o, cartItem, "id");
                    cartItem.setCreateTime(LocalDateTime.now());
                    cartItem.setUserId(userId);
                    return cartItem;
                })
                .collect(Collectors.toList());
        cartItemMapper.insertBatch(cartItemList);
    }

    @Override
    @Transactional
    public void cancel(Long id) throws Exception {
        Orders orders = ordersMapper.getById(id);
        Orders updateOrders = new Orders();
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (orders.getStatus() == 2) {
            //退款(微信退款先注释掉）
//            weChatPayUtil.refund(orders.getNumber(),orders.getNumber() ,new BigDecimal(0.01), new BigDecimal(0.01));
            updateOrders.setPayStatus(Orders.REFUND);
        }
        releaseLockedStock(id);
        updateOrders.setStatus(Orders.CANCELLED);
        updateOrders.setId(orders.getId());
        updateOrders.setCancelReason("用户取消订单");
        updateOrders.setCancelTime(LocalDateTime.now());
        ordersMapper.updateById(updateOrders);
        fulfillmentTaskService.markCancelledByOrderId(orders.getId());
    }

    @Override
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(ordersMapper.countByStatus(2));
        orderStatisticsVO.setConfirmed(ordersMapper.countByStatus(3));
        orderStatisticsVO.setReadyForPickup(ordersMapper.countByStatus(Orders.READY_FOR_PICKUP));

        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(3)
                .build();
        ordersMapper.updateById(orders);
    }

    @Override
    @Transactional
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Orders orders = ordersMapper.getById(ordersRejectionDTO.getId());
        if (orders.getStatus() != 2) {
            throw new OrderBusinessException(MessageConstant.WRONG_ORDER_STATUS);
        }
        releaseLockedStock(ordersRejectionDTO.getId());
        Orders updateOrders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(6)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .build();
        ordersMapper.updateById(updateOrders);
        fulfillmentTaskService.markCancelledByOrderId(ordersRejectionDTO.getId());

//        退款：
//        weChatPayUtil.refund(orders.getNumber(), orders.getNumber(), new BigDecimal(0.01), new BigDecimal(0.01));

    }

    @Override
    @Transactional
    public void cancelByAdmin(OrdersCancelDTO ordersCancelDTO) {
        releaseLockedStock(ordersCancelDTO.getId());
        Orders updateOrders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(6)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build();
        Orders orders = ordersMapper.getById(ordersCancelDTO.getId());
        ordersMapper.updateById(updateOrders);
        fulfillmentTaskService.markCancelledByOrderId(ordersCancelDTO.getId());

        if (orders.getStatus() != 1) {//已付款
            //        退款：
//        weChatPayUtil.refund(orders.getNumber(), orders.getNumber(), new BigDecimal(0.01), new BigDecimal(0.01));
        }

    }

    @Override
    public void readyForPickup(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders.getStatus() != 3) {
            throw new OrderBusinessException(MessageConstant.WRONG_ORDER_STATUS);
        }
        Orders updateOrders = Orders.builder()
                .id(id)
                .status(Orders.READY_FOR_PICKUP)
                .build();
        ordersMapper.updateById(updateOrders);
        fulfillmentTaskService.markReadyByOrderId(id);

    }

    @Override
    @Transactional
    public void verify(OrdersVerifyDTO ordersVerifyDTO) {
        Orders orders = ordersMapper.getById(ordersVerifyDTO.getOrderId());
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (orders.getStatus() != Orders.READY_FOR_PICKUP) {
            throw new OrderBusinessException(MessageConstant.WRONG_ORDER_STATUS);
        }
        if (ordersVerifyDTO.getPickupCode() == null || !ordersVerifyDTO.getPickupCode().equals(orders.getPickupCode())) {
            throw new OrderBusinessException("自提核销码错误");
        }
        completeOrder(orders.getId());
    }

    @Override
    @Transactional
    public void complete(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (orders.getStatus() != Orders.READY_FOR_PICKUP) {
            throw new OrderBusinessException(MessageConstant.WRONG_ORDER_STATUS);
        }
        completeOrder(id);
    }

    private void completeOrder(Long id) {
        consumeLockedStock(id);
        LocalDateTime now = LocalDateTime.now();
        Orders updateOrders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .completedTime(now)
                .verifyTime(now)
                .verifyStaffId(CurrentActorContext.getCurrentId())
                .build();
        ordersMapper.updateById(updateOrders);
        fulfillmentTaskService.markCompletedByOrderId(id);
    }

    private void lockBatchStock(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            if (cartItem.getBatchId() == null) {
                continue;
            }
            int updated = inventoryBatchMapper.lockStock(cartItem.getBatchId(), cartItem.getNumber());
            if (updated != 1) {
                throw new OrderBusinessException(MessageConstant.INVENTORY_NOT_ENOUGH);
            }
        }
    }

    private void releaseLockedStock(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
        for (OrderDetail orderDetail : orderDetails) {
            if (orderDetail.getBatchId() == null) {
                continue;
            }
            int updated = inventoryBatchMapper.releaseLockedStock(orderDetail.getBatchId(), orderDetail.getNumber());
            if (updated != 1) {
                throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
            }
        }
    }

    private void consumeLockedStock(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
        for (OrderDetail orderDetail : orderDetails) {
            if (orderDetail.getBatchId() == null) {
                continue;
            }
            int updated = inventoryBatchMapper.consumeLockedStock(orderDetail.getBatchId(), orderDetail.getNumber());
            if (updated != 1) {
                throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
            }
        }
    }

    @Override
    public void reminder(Long id) {
        Orders orders = ordersMapper.getById(id);
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", id);
        map.put("content", "订单号:"+orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    private String generatePickupCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
    }

}
