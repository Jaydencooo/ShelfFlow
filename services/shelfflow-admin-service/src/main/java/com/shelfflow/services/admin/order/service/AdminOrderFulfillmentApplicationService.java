package com.shelfflow.services.admin.order.service;

import com.shelfflow.services.admin.order.domain.AdminOrderFulfillmentPolicy;
import com.shelfflow.services.admin.order.persistence.AdminOrderPersistenceMapper;
import com.shelfflow.services.admin.order.persistence.dataobject.AdminOrderDetailRow;
import com.shelfflow.services.admin.order.persistence.dataobject.AdminOrderEventRow;
import com.shelfflow.services.admin.order.persistence.dataobject.AdminOrderItemRow;
import com.shelfflow.services.admin.order.persistence.dataobject.AdminOrderPageCriteria;
import com.shelfflow.services.admin.order.persistence.dataobject.AdminOrderSummaryRow;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.domain.OrderEventActorType;
import com.shelfflow.services.common.domain.OrderEventType;
import com.shelfflow.services.common.domain.UserOrderPayStatus;
import com.shelfflow.services.common.domain.UserOrderStatus;
import com.shelfflow.services.common.dto.AdminOrderDetailResponse;
import com.shelfflow.services.common.dto.AdminOrderItemResponse;
import com.shelfflow.services.common.dto.AdminOrderQuery;
import com.shelfflow.services.common.dto.AdminOrderSummaryResponse;
import com.shelfflow.services.common.dto.OrderEventResponse;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminOrderFulfillmentApplicationService {

    private final AdminOrderPersistenceMapper adminOrderPersistenceMapper;
    private final AdminOrderFulfillmentPolicy adminOrderFulfillmentPolicy;

    public AdminOrderFulfillmentApplicationService(AdminOrderPersistenceMapper adminOrderPersistenceMapper,
                                                   AdminOrderFulfillmentPolicy adminOrderFulfillmentPolicy) {
        this.adminOrderPersistenceMapper = adminOrderPersistenceMapper;
        this.adminOrderFulfillmentPolicy = adminOrderFulfillmentPolicy;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminOrderSummaryResponse> page(AdminOrderQuery query) {
        UserOrderStatus status = adminOrderFulfillmentPolicy.parseOptionalStatus(query.getStatus());
        UserOrderPayStatus payStatus = adminOrderFulfillmentPolicy.parseOptionalPayStatus(query.getPayStatus());
        AdminOrderPageCriteria criteria = AdminOrderPageCriteria.builder()
                .keyword(blankToNull(query.getKeyword()))
                .status(status == null ? null : status.legacyValue())
                .payStatus(payStatus == null ? null : payStatus.legacyValue())
                .offset((query.getPage() - 1) * query.getPageSize())
                .pageSize(query.getPageSize())
                .sortColumn(adminOrderFulfillmentPolicy.resolveSortColumn(query.getSortBy()))
                .sortOrder(query.getSortOrder().name())
                .build();

        List<AdminOrderSummaryResponse> items = adminOrderPersistenceMapper.pageOrders(criteria).stream()
                .map(this::toSummaryResponse)
                .toList();

        return PageResponse.<AdminOrderSummaryResponse>builder()
                .items(items)
                .total(adminOrderPersistenceMapper.countOrders(criteria))
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailResponse getById(String orderId) {
        Long id = adminOrderFulfillmentPolicy.parseRequiredOrderId(orderId);
        AdminOrderDetailRow row = adminOrderPersistenceMapper.findOrderById(id);
        adminOrderFulfillmentPolicy.ensureOrderExists(row != null);
        List<AdminOrderItemResponse> items = adminOrderPersistenceMapper.listOrderItemsByOrderId(id).stream()
                .map(this::toItemResponse)
                .toList();
        List<OrderEventResponse> events = adminOrderPersistenceMapper.listOrderEventsByOrderId(id).stream()
                .map(this::toEventResponse)
                .toList();
        return toDetailResponse(row, items, events);
    }

    @Transactional
    public AdminOrderDetailResponse updateStatus(Long actorId, String orderId, UserOrderStatus targetStatus) {
        if (targetStatus == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "目标订单状态不能为空");
        }
        Long id = adminOrderFulfillmentPolicy.parseRequiredOrderId(orderId);
        AdminOrderDetailRow row = adminOrderPersistenceMapper.findOrderById(id);
        adminOrderFulfillmentPolicy.ensureOrderExists(row != null);

        UserOrderStatus currentStatus = UserOrderStatus.fromLegacy(row.getStatus());
        UserOrderPayStatus payStatus = UserOrderPayStatus.fromLegacy(row.getPayStatus());
        adminOrderFulfillmentPolicy.ensureAdminTransitionAllowed(currentStatus, targetStatus, payStatus);

        int affectedRows = adminOrderPersistenceMapper.updateOrderStatus(id, currentStatus.legacyValue(), targetStatus.legacyValue());
        if (affectedRows <= 0) {
            throw new ApplicationException(ErrorCode.CONFLICT, "订单状态已变化，请刷新后重试");
        }

        LocalDateTime eventTime = LocalDateTime.now();
        if (adminOrderFulfillmentPolicy.requiresInventorySettlement(targetStatus)) {
            settleInventory(id);
        }
        adminOrderPersistenceMapper.insertOrderEvent(buildOrderEvent(
                id,
                actorId,
                OrderEventType.FULFILLMENT_UPDATED,
                currentStatus,
                targetStatus,
                payStatus,
                payStatus,
                String.format("管理员将订单状态从 %s 更新为 %s", currentStatus.value(), targetStatus.value()),
                eventTime
        ));
        return getById(String.valueOf(id));
    }

    @Transactional
    public AdminOrderDetailResponse verifyPickup(Long actorId, String orderId, String pickupCode) {
        Long id = adminOrderFulfillmentPolicy.parseRequiredOrderId(orderId);
        AdminOrderDetailRow row = adminOrderPersistenceMapper.findOrderById(id);
        adminOrderFulfillmentPolicy.ensureOrderExists(row != null);

        LocalDateTime eventTime = LocalDateTime.now();
        UserOrderStatus currentStatus = UserOrderStatus.fromLegacy(row.getStatus());
        UserOrderPayStatus payStatus = UserOrderPayStatus.fromLegacy(row.getPayStatus());
        adminOrderFulfillmentPolicy.ensurePickupVerificationAllowed(
                currentStatus,
                payStatus,
                row.getPickupCode(),
                pickupCode,
                row.getPickupDeadline(),
                eventTime
        );

        int affectedRows = adminOrderPersistenceMapper.updateOrderStatus(
                id,
                currentStatus.legacyValue(),
                UserOrderStatus.COMPLETED.legacyValue()
        );
        if (affectedRows <= 0) {
            throw new ApplicationException(ErrorCode.CONFLICT, "订单状态已变化，请刷新后重试");
        }

        settleInventory(id);
        adminOrderPersistenceMapper.insertOrderEvent(buildOrderEvent(
                id,
                actorId,
                OrderEventType.FULFILLMENT_UPDATED,
                currentStatus,
                UserOrderStatus.COMPLETED,
                payStatus,
                payStatus,
                "管理员核销自提码并完成订单",
                eventTime
        ));
        return getById(String.valueOf(id));
    }

    private void settleInventory(Long orderId) {
        LocalDateTime now = LocalDateTime.now();
        for (AdminOrderItemRow item : adminOrderPersistenceMapper.listOrderItemsByOrderId(orderId)) {
            if (item.getBatchId() == null) {
                continue;
            }
            int affectedRows = adminOrderPersistenceMapper.settleBatchStock(item.getBatchId(), item.getNumber(), now);
            adminOrderFulfillmentPolicy.ensureInventorySettled(affectedRows > 0);
        }
    }

    private AdminOrderSummaryResponse toSummaryResponse(AdminOrderSummaryRow row) {
        return AdminOrderSummaryResponse.builder()
                .id(String.valueOf(row.getId()))
                .orderNumber(row.getNumber())
                .userId(String.valueOf(row.getUserId()))
                .userName(row.getUserName())
                .consignee(row.getConsignee())
                .phone(row.getPhone())
                .status(UserOrderStatus.fromLegacy(row.getStatus()))
                .payStatus(UserOrderPayStatus.fromLegacy(row.getPayStatus()))
                .totalAmount(row.getAmount())
                .pickupCode(row.getPickupCode())
                .orderTime(row.getOrderTime())
                .checkoutTime(row.getCheckoutTime())
                .pickupDeadline(row.getPickupDeadline())
                .itemCount(row.getItemCount() == null ? 0 : row.getItemCount())
                .build();
    }

    private AdminOrderDetailResponse toDetailResponse(AdminOrderDetailRow row, List<AdminOrderItemResponse> items, List<OrderEventResponse> events) {
        return AdminOrderDetailResponse.builder()
                .id(String.valueOf(row.getId()))
                .orderNumber(row.getNumber())
                .userId(String.valueOf(row.getUserId()))
                .userName(row.getUserName())
                .consignee(row.getConsignee())
                .phone(row.getPhone())
                .status(UserOrderStatus.fromLegacy(row.getStatus()))
                .payStatus(UserOrderPayStatus.fromLegacy(row.getPayStatus()))
                .totalAmount(row.getAmount())
                .remark(row.getRemark())
                .pickupPoint(row.getPickupPoint())
                .pickupCode(row.getPickupCode())
                .orderTime(row.getOrderTime())
                .checkoutTime(row.getCheckoutTime())
                .pickupDeadline(row.getPickupDeadline())
                .cancelTime(row.getCancelTime())
                .cancelReason(row.getCancelReason())
                .itemCount(items.stream().mapToInt(AdminOrderItemResponse::getQuantity).sum())
                .items(items)
                .events(events)
                .build();
    }

    private AdminOrderItemResponse toItemResponse(AdminOrderItemRow row) {
        return AdminOrderItemResponse.builder()
                .productId(row.getProductId() == null ? "" : String.valueOf(row.getProductId()))
                .batchId(row.getBatchId() == null ? "" : String.valueOf(row.getBatchId()))
                .name(row.getName())
                .image(row.getImage())
                .productSpec(row.getProductSpec())
                .quantity(row.getNumber())
                .lineAmount(row.getAmount())
                .unitPrice(calculateUnitPrice(row.getAmount(), row.getNumber()))
                .build();
    }

    private BigDecimal calculateUnitPrice(BigDecimal lineAmount, Integer quantity) {
        if (lineAmount == null || quantity == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }
        return lineAmount.divide(BigDecimal.valueOf(quantity), 2, java.math.RoundingMode.HALF_UP);
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private AdminOrderEventRow buildOrderEvent(Long orderId,
                                               Long actorId,
                                               OrderEventType eventType,
                                               UserOrderStatus fromStatus,
                                               UserOrderStatus toStatus,
                                               UserOrderPayStatus fromPayStatus,
                                               UserOrderPayStatus toPayStatus,
                                               String note,
                                               LocalDateTime eventTime) {
        AdminOrderEventRow event = new AdminOrderEventRow();
        event.setOrderId(orderId);
        event.setEventType(eventType.value());
        event.setActorType(OrderEventActorType.ADMIN.value());
        event.setActorId(actorId);
        event.setFromStatus(fromStatus == null ? null : fromStatus.legacyValue());
        event.setToStatus(toStatus == null ? null : toStatus.legacyValue());
        event.setFromPayStatus(fromPayStatus == null ? null : fromPayStatus.legacyValue());
        event.setToPayStatus(toPayStatus == null ? null : toPayStatus.legacyValue());
        event.setNote(note);
        event.setCreateTime(eventTime);
        return event;
    }

    private OrderEventResponse toEventResponse(AdminOrderEventRow row) {
        return OrderEventResponse.builder()
                .id(String.valueOf(row.getId()))
                .eventType(OrderEventType.fromValue(row.getEventType()))
                .actorType(OrderEventActorType.fromValue(row.getActorType()))
                .actorId(row.getActorId() == null ? null : String.valueOf(row.getActorId()))
                .fromStatus(row.getFromStatus() == null ? null : UserOrderStatus.fromLegacy(row.getFromStatus()))
                .toStatus(row.getToStatus() == null ? null : UserOrderStatus.fromLegacy(row.getToStatus()))
                .fromPayStatus(row.getFromPayStatus() == null ? null : UserOrderPayStatus.fromLegacy(row.getFromPayStatus()))
                .toPayStatus(row.getToPayStatus() == null ? null : UserOrderPayStatus.fromLegacy(row.getToPayStatus()))
                .note(row.getNote())
                .eventTime(row.getCreateTime())
                .build();
    }
}
