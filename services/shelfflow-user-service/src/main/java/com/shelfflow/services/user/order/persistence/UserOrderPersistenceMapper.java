package com.shelfflow.services.user.order.persistence;

import com.shelfflow.services.user.order.persistence.dataobject.UserOrderCartItemRow;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderDataObject;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderDetailRow;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderDetailDataObject;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderEventDataObject;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderItemRow;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderPageCriteria;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderSummaryRow;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderTimeoutCandidateRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserOrderPersistenceMapper {

    List<UserOrderCartItemRow> listCheckoutItemsByUserId(@Param("userId") Long userId);

    List<UserOrderCartItemRow> listCheckoutItemsByUserIdAndCartItemIds(@Param("userId") Long userId,
                                                                       @Param("cartItemIds") List<Long> cartItemIds);

    int incrementBatchLockedQuantity(@Param("batchId") Long batchId,
                                     @Param("quantity") Integer quantity,
                                     @Param("updateTime") LocalDateTime updateTime);

    int decrementBatchLockedQuantity(@Param("batchId") Long batchId,
                                     @Param("quantity") Integer quantity,
                                     @Param("updateTime") LocalDateTime updateTime);

    void insertOrder(UserOrderDataObject order);

    void insertOrderDetails(@Param("items") List<UserOrderDetailDataObject> items);

    List<UserOrderSummaryRow> pageOrders(UserOrderPageCriteria criteria);

    long countOrders(UserOrderPageCriteria criteria);

    List<UserOrderItemRow> listOrderItemsByOrderIds(@Param("orderIds") List<Long> orderIds);

    List<UserOrderItemRow> listOrderItemsByOrderId(@Param("orderId") Long orderId);

    List<UserOrderEventDataObject> listOrderEventsByOrderId(@Param("orderId") Long orderId);

    UserOrderDetailRow findOrderByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<UserOrderTimeoutCandidateRow> listTimeoutCloseCandidates(@Param("deadline") LocalDateTime deadline,
                                                                  @Param("status") Integer status,
                                                                  @Param("payStatus") Integer payStatus,
                                                                  @Param("limit") Integer limit);

    void insertOrderEvent(UserOrderEventDataObject orderEvent);

    int cancelOrder(@Param("id") Long id,
                    @Param("status") Integer status,
                    @Param("cancelReason") String cancelReason,
                    @Param("cancelTime") LocalDateTime cancelTime);

    int payOrder(@Param("id") Long id,
                 @Param("status") Integer status,
                 @Param("payStatus") Integer payStatus,
                 @Param("checkoutTime") LocalDateTime checkoutTime);

    int timeoutCancelOrder(@Param("id") Long id,
                           @Param("pendingStatus") Integer pendingStatus,
                           @Param("unpaidPayStatus") Integer unpaidPayStatus,
                           @Param("cancelledStatus") Integer cancelledStatus,
                           @Param("cancelReason") String cancelReason,
                           @Param("cancelTime") LocalDateTime cancelTime,
                           @Param("deadline") LocalDateTime deadline);
}
