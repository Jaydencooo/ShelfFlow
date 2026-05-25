package com.shelfflow.service;


import com.shelfflow.dto.*;
import com.shelfflow.result.PageResult;
import com.shelfflow.vo.OrderPaymentVO;
import com.shelfflow.vo.OrderStatisticsVO;
import com.shelfflow.vo.OrderSubmitVO;
import com.shelfflow.vo.OrderVO;

public interface OrderService {

    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    void paySuccess(String outTradeNo);

    PageResult ordersPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO getById(Long id);

    void repetition(Long id);

    void cancel(Long id) throws Exception;

    OrderStatisticsVO getStatistics();

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    void cancelByAdmin(OrdersCancelDTO ordersCancelDTO);

    void readyForPickup(Long id);

    void verify(OrdersVerifyDTO ordersVerifyDTO);

    void complete(Long id);

    void reminder(Long id);
}
