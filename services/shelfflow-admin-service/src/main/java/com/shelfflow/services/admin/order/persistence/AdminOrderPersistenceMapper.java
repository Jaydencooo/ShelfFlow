package com.shelfflow.services.admin.order.persistence;

import com.shelfflow.services.admin.order.persistence.dataobject.AdminOrderDetailRow;
import com.shelfflow.services.admin.order.persistence.dataobject.AdminOrderEventRow;
import com.shelfflow.services.admin.order.persistence.dataobject.AdminOrderItemRow;
import com.shelfflow.services.admin.order.persistence.dataobject.AdminOrderPageCriteria;
import com.shelfflow.services.admin.order.persistence.dataobject.AdminOrderSummaryRow;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminOrderPersistenceMapper {
    List<AdminOrderSummaryRow> pageOrders(AdminOrderPageCriteria criteria);

    long countOrders(AdminOrderPageCriteria criteria);

    AdminOrderDetailRow findOrderById(@Param("id") Long id);

    List<AdminOrderItemRow> listOrderItemsByOrderId(@Param("orderId") Long orderId);

    List<AdminOrderEventRow> listOrderEventsByOrderId(@Param("orderId") Long orderId);

    int updateOrderStatus(@Param("id") Long id,
                          @Param("expectedStatus") Integer expectedStatus,
                          @Param("targetStatus") Integer targetStatus);

    int settleBatchStock(@Param("batchId") Long batchId,
                         @Param("quantity") Integer quantity,
                         @Param("updateTime") LocalDateTime updateTime);

    void insertOrderEvent(AdminOrderEventRow orderEvent);
}
