package com.shelfflow.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.shelfflow.dto.FulfillmentTaskPageQueryDTO;
import com.shelfflow.entity.FulfillmentTask;
import com.shelfflow.entity.Orders;
import com.shelfflow.mapper.FulfillmentTaskMapper;
import com.shelfflow.result.PageResult;
import com.shelfflow.service.FulfillmentTaskService;
import com.shelfflow.vo.FulfillmentTaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FulfillmentTaskServiceImpl implements FulfillmentTaskService {

    @Autowired
    private FulfillmentTaskMapper fulfillmentTaskMapper;

    @Override
    public void createForOrder(Orders orders) {
        if (orders == null || orders.getId() == null) {
            return;
        }
        FulfillmentTask task = FulfillmentTask.builder()
                .orderId(orders.getId())
                .orderNumber(orders.getNumber())
                .pickupCode(orders.getPickupCode())
                .status(FulfillmentTask.WAITING_PREPARE)
                .pickupDeadline(orders.getPickupDeadline())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        fulfillmentTaskMapper.upsert(task);
    }

    @Override
    public void markReadyByOrderId(Long orderId) {
        updateStatus(orderId, FulfillmentTask.READY_FOR_PICKUP, null);
    }

    @Override
    public void markCompletedByOrderId(Long orderId) {
        updateStatus(orderId, FulfillmentTask.COMPLETED, LocalDateTime.now());
    }

    @Override
    public void markCancelledByOrderId(Long orderId) {
        updateStatus(orderId, FulfillmentTask.CANCELLED, null);
    }

    @Override
    public PageResult pageQuery(FulfillmentTaskPageQueryDTO fulfillmentTaskPageQueryDTO) {
        PageHelper.startPage(fulfillmentTaskPageQueryDTO.getPage(), fulfillmentTaskPageQueryDTO.getPageSize());
        Page<FulfillmentTaskVO> page = fulfillmentTaskMapper.pageQuery(fulfillmentTaskPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public FulfillmentTaskVO getByOrderId(Long orderId) {
        return fulfillmentTaskMapper.getByOrderId(orderId);
    }

    @Override
    public Map<String, Long> statusStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("waitingPrepare", 0L);
        stats.put("readyForPickup", 0L);
        stats.put("completed", 0L);
        stats.put("cancelled", 0L);
        stats.put("total", 0L);

        List<Map<String, Object>> rows = fulfillmentTaskMapper.statusStatistics();
        if (rows == null || rows.isEmpty()) {
            return stats;
        }

        long total = 0L;
        for (Map<String, Object> row : rows) {
            Integer status = row.get("status") == null ? null : Integer.parseInt(String.valueOf(row.get("status")));
            Long count = row.get("total") == null ? 0L : Long.parseLong(String.valueOf(row.get("total")));
            total += count;
            if (status == null) {
                continue;
            }
            if (status.equals(FulfillmentTask.WAITING_PREPARE)) {
                stats.put("waitingPrepare", count);
            } else if (status.equals(FulfillmentTask.READY_FOR_PICKUP)) {
                stats.put("readyForPickup", count);
            } else if (status.equals(FulfillmentTask.COMPLETED)) {
                stats.put("completed", count);
            } else if (status.equals(FulfillmentTask.CANCELLED)) {
                stats.put("cancelled", count);
            }
        }
        stats.put("total", total);
        return stats;
    }

    private void updateStatus(Long orderId, Integer status, LocalDateTime completedTime) {
        FulfillmentTask task = FulfillmentTask.builder()
                .orderId(orderId)
                .status(status)
                .completedTime(completedTime)
                .updateTime(LocalDateTime.now())
                .build();
        fulfillmentTaskMapper.updateByOrderId(task);
    }
}
