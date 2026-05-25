package com.shelfflow.service;

import com.shelfflow.dto.FulfillmentTaskPageQueryDTO;
import com.shelfflow.entity.Orders;
import com.shelfflow.result.PageResult;
import com.shelfflow.vo.FulfillmentTaskVO;

import java.util.Map;

public interface FulfillmentTaskService {

    void createForOrder(Orders orders);

    void markReadyByOrderId(Long orderId);

    void markCompletedByOrderId(Long orderId);

    void markCancelledByOrderId(Long orderId);

    PageResult pageQuery(FulfillmentTaskPageQueryDTO fulfillmentTaskPageQueryDTO);

    FulfillmentTaskVO getByOrderId(Long orderId);

    Map<String, Long> statusStatistics();
}
