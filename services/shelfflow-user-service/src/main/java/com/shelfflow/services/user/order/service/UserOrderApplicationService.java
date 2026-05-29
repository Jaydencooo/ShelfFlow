package com.shelfflow.services.user.order.service;

import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.domain.OrderEventActorType;
import com.shelfflow.services.common.domain.OrderEventType;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.common.domain.UserOrderPaymentStatus;
import com.shelfflow.services.common.domain.UserOrderPayStatus;
import com.shelfflow.services.common.domain.UserOrderStatus;
import com.shelfflow.services.common.dto.OrderEventResponse;
import com.shelfflow.services.common.dto.UserOrderDetailResponse;
import com.shelfflow.services.common.dto.UserOrderCancelRequest;
import com.shelfflow.services.common.dto.UserOrderItemResponse;
import com.shelfflow.services.common.dto.UserOrderQuery;
import com.shelfflow.services.common.dto.UserOrderSubmitRequest;
import com.shelfflow.services.common.dto.UserOrderSubmitResponse;
import com.shelfflow.services.common.dto.UserOrderSummaryResponse;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.user.auth.persistence.UserAccountPersistenceMapper;
import com.shelfflow.services.user.auth.persistence.dataobject.UserAccountDataObject;
import com.shelfflow.services.user.cart.persistence.UserCartPersistenceMapper;
import com.shelfflow.services.user.config.UserOrderProperties;
import com.shelfflow.services.user.order.domain.UserOrderPolicy;
import com.shelfflow.services.user.order.messaging.UserOrderEventMessage;
import com.shelfflow.services.user.order.messaging.UserOrderEventPublisher;
import com.shelfflow.services.user.order.persistence.UserOrderPersistenceMapper;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderCartItemRow;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderDataObject;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderDetailRow;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderDetailDataObject;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderEventDataObject;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderItemRow;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderPageCriteria;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderPaymentDataObject;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderSummaryRow;
import com.shelfflow.services.user.pickupcontact.domain.UserPickupContactPolicy;
import com.shelfflow.services.user.pickupcontact.persistence.UserPickupContactPersistenceMapper;
import com.shelfflow.services.user.pickupcontact.persistence.dataobject.UserPickupContactDataObject;
import com.shelfflow.services.user.pickuppoint.persistence.UserPickupPointPersistenceMapper;
import com.shelfflow.services.user.pickuppoint.persistence.dataobject.UserPickupPointDataObject;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserOrderApplicationService {

    private static final String PAYMENT_NO_PREFIX = "PAY";
    private static final String PAYMENT_IDEMPOTENCY_PREFIX = "user-order-pay";
    private static final String PAYMENT_IDEMPOTENCY_SEPARATOR = ":";
    private static final String DEFAULT_PAYMENT_PROVIDER = "mock";

    private final UserOrderPersistenceMapper userOrderPersistenceMapper;
    private final UserCartPersistenceMapper userCartPersistenceMapper;
    private final UserAccountPersistenceMapper userAccountPersistenceMapper;
    private final UserPickupContactPersistenceMapper userPickupContactPersistenceMapper;
    private final UserPickupPointPersistenceMapper userPickupPointPersistenceMapper;
    private final UserOrderPolicy userOrderPolicy;
    private final UserPickupContactPolicy userPickupContactPolicy;
    private final UserOrderProperties userOrderProperties;
    private final UserInventoryReservationService userInventoryReservationService;
    private final UserOrderEventPublisher userOrderEventPublisher;

    public UserOrderApplicationService(UserOrderPersistenceMapper userOrderPersistenceMapper,
                                       UserCartPersistenceMapper userCartPersistenceMapper,
                                       UserAccountPersistenceMapper userAccountPersistenceMapper,
                                       UserPickupContactPersistenceMapper userPickupContactPersistenceMapper,
                                       UserPickupPointPersistenceMapper userPickupPointPersistenceMapper,
                                       UserOrderPolicy userOrderPolicy,
                                       UserPickupContactPolicy userPickupContactPolicy,
                                       UserOrderProperties userOrderProperties,
                                       UserInventoryReservationService userInventoryReservationService,
                                       UserOrderEventPublisher userOrderEventPublisher) {
        this.userOrderPersistenceMapper = userOrderPersistenceMapper;
        this.userCartPersistenceMapper = userCartPersistenceMapper;
        this.userAccountPersistenceMapper = userAccountPersistenceMapper;
        this.userPickupContactPersistenceMapper = userPickupContactPersistenceMapper;
        this.userPickupPointPersistenceMapper = userPickupPointPersistenceMapper;
        this.userOrderPolicy = userOrderPolicy;
        this.userPickupContactPolicy = userPickupContactPolicy;
        this.userOrderProperties = userOrderProperties;
        this.userInventoryReservationService = userInventoryReservationService;
        this.userOrderEventPublisher = userOrderEventPublisher;
    }

    @Transactional
    public UserOrderSubmitResponse submit(UserAuthenticatedUser authenticatedUser, UserOrderSubmitRequest request) {
        List<Long> selectedCartItemIds = userOrderPolicy.parseOptionalCartItemIds(request.getCartItemIds());
        List<UserOrderCartItemRow> cartItems = selectedCartItemIds == null
                ? userOrderPersistenceMapper.listCheckoutItemsByUserId(authenticatedUser.getUserId())
                : userOrderPersistenceMapper.listCheckoutItemsByUserIdAndCartItemIds(authenticatedUser.getUserId(), selectedCartItemIds);
        userOrderPolicy.ensureCartNotEmpty(cartItems);
        if (selectedCartItemIds != null) {
            userOrderPolicy.ensureSelectedCartItemsMatched(selectedCartItemIds.size(), cartItems.size());
        }
        userOrderPolicy.ensureCartItemsEligibleForSubmit(cartItems);

        LocalDateTime now = LocalDateTime.now();
        List<UserInventoryReservationService.ReservedInventoryItem> reservedInventoryItems =
                userInventoryReservationService.reserve(cartItems);
        boolean releaseRegistered = false;
        try {
            for (UserOrderCartItemRow cartItem : cartItems) {
                int affectedRows = userOrderPersistenceMapper.incrementBatchLockedQuantity(
                        cartItem.getBatchId(),
                        cartItem.getQuantity(),
                        now
                );
                userOrderPolicy.ensureStockLocked(affectedRows > 0);
            }
            registerInventoryReservationRelease(reservedInventoryItems);
            releaseRegistered = !reservedInventoryItems.isEmpty();
        } catch (RuntimeException ex) {
            if (!releaseRegistered) {
                userInventoryReservationService.release(reservedInventoryItems);
            }
            throw ex;
        }

        UserAccountDataObject user = userAccountPersistenceMapper.findById(authenticatedUser.getUserId());
        UserPickupContactDataObject pickupContact = resolvePickupContact(authenticatedUser, request);
        UserPickupPointDataObject pickupPoint = resolvePickupPoint(request);
        userOrderPolicy.ensurePickupConsigneePresent(resolveOrderConsignee(user, pickupContact));
        userOrderPolicy.ensurePickupPhonePresent(resolveOrderPhone(user, pickupContact));
        UserOrderDataObject order = buildOrder(authenticatedUser, request, cartItems, user, pickupContact, pickupPoint, now);
        userOrderPersistenceMapper.insertOrder(order);
        userOrderPersistenceMapper.insertOrderDetails(buildOrderDetails(order.getId(), cartItems));
        insertAndPublishOrderEvent(buildOrderEvent(
                order.getId(),
                OrderEventType.SUBMITTED,
                OrderEventActorType.USER,
                authenticatedUser.getUserId(),
                null,
                UserOrderStatus.PENDING_PAYMENT,
                null,
                UserOrderPayStatus.UNPAID,
                "用户提交订单",
                now
        ), order.getNumber(), authenticatedUser.getUserId(), order.getAmount(), userOrderPolicy.calculateItemCount(cartItems));
        if (selectedCartItemIds == null) {
            userCartPersistenceMapper.clearByUserId(authenticatedUser.getUserId());
        } else {
            userCartPersistenceMapper.deleteByIdsAndUserId(selectedCartItemIds, authenticatedUser.getUserId());
        }

        return UserOrderSubmitResponse.builder()
                .id(String.valueOf(order.getId()))
                .orderNumber(order.getNumber())
                .status(UserOrderStatus.fromLegacy(order.getStatus()))
                .payStatus(UserOrderPayStatus.fromLegacy(order.getPayStatus()))
                .totalAmount(order.getAmount())
                .itemCount(userOrderPolicy.calculateItemCount(cartItems))
                .pickupCode(order.getPickupCode())
                .orderTime(order.getOrderTime())
                .pickupDeadline(order.getPickupDeadline())
                .build();
    }

    private void registerInventoryReservationRelease(
            List<UserInventoryReservationService.ReservedInventoryItem> reservedInventoryItems) {
        if (reservedInventoryItems == null || reservedInventoryItems.isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            userInventoryReservationService.release(reservedInventoryItems);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                userInventoryReservationService.release(reservedInventoryItems);
            }
        });
    }

    public PageResponse<UserOrderSummaryResponse> pageOrders(UserAuthenticatedUser authenticatedUser, UserOrderQuery query) {
        UserOrderStatus status = userOrderPolicy.parseOptionalStatus(query.getStatus());
        UserOrderPageCriteria criteria = UserOrderPageCriteria.builder()
                .userId(authenticatedUser.getUserId())
                .status(status == null ? null : status.legacyValue())
                .offset((query.getPage() - 1) * query.getPageSize())
                .pageSize(query.getPageSize())
                .sortColumn(userOrderPolicy.resolveSortColumn(query.getSortBy()))
                .sortOrder(query.getSortOrder().name())
                .build();

        List<UserOrderSummaryRow> rows = userOrderPersistenceMapper.pageOrders(criteria);
        long total = userOrderPersistenceMapper.countOrders(criteria);
        Map<Long, List<UserOrderItemResponse>> itemsByOrderId = resolveOrderItems(rows);

        List<UserOrderSummaryResponse> items = rows.stream()
                .map(row -> {
                    List<UserOrderItemResponse> orderItems = itemsByOrderId.getOrDefault(row.getId(), List.of());
                    return UserOrderSummaryResponse.builder()
                            .id(String.valueOf(row.getId()))
                            .orderNumber(row.getNumber())
                            .status(UserOrderStatus.fromLegacy(row.getStatus()))
                            .payStatus(UserOrderPayStatus.fromLegacy(row.getPayStatus()))
                            .totalAmount(row.getAmount())
                            .remark(row.getRemark())
                            .pickupCode(row.getPickupCode())
                            .orderTime(row.getOrderTime())
                            .pickupDeadline(row.getPickupDeadline())
                            .itemCount(orderItems.stream().mapToInt(UserOrderItemResponse::getQuantity).sum())
                            .items(orderItems)
                            .build();
                })
                .toList();

        return PageResponse.<UserOrderSummaryResponse>builder()
                .items(items)
                .total(total)
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }

    public UserOrderDetailResponse getOrderDetail(UserAuthenticatedUser authenticatedUser, String orderId) {
        Long id = userOrderPolicy.parseRequiredOrderId(orderId);
        UserOrderDetailRow row = userOrderPersistenceMapper.findOrderByIdAndUserId(id, authenticatedUser.getUserId());
        userOrderPolicy.ensureOrderExists(row != null);
        List<UserOrderItemResponse> items = userOrderPersistenceMapper.listOrderItemsByOrderId(id).stream()
                .map(this::toItemResponse)
                .toList();
        List<OrderEventResponse> events = userOrderPersistenceMapper.listOrderEventsByOrderId(id).stream()
                .map(this::toEventResponse)
                .toList();
        return UserOrderDetailResponse.builder()
                .id(String.valueOf(row.getId()))
                .orderNumber(row.getNumber())
                .status(UserOrderStatus.fromLegacy(row.getStatus()))
                .payStatus(UserOrderPayStatus.fromLegacy(row.getPayStatus()))
                .totalAmount(row.getAmount())
                .remark(row.getRemark())
                .phone(row.getPhone())
                .pickupPoint(row.getPickupPoint())
                .consignee(row.getConsignee())
                .pickupCode(row.getPickupCode())
                .orderTime(row.getOrderTime())
                .checkoutTime(row.getCheckoutTime())
                .pickupDeadline(row.getPickupDeadline())
                .cancelTime(row.getCancelTime())
                .cancelReason(row.getCancelReason())
                .itemCount(items.stream().mapToInt(UserOrderItemResponse::getQuantity).sum())
                .items(items)
                .events(events)
                .build();
    }

    @Transactional
    public void cancelOrder(UserAuthenticatedUser authenticatedUser, String orderId, UserOrderCancelRequest request) {
        Long id = userOrderPolicy.parseRequiredOrderId(orderId);
        UserOrderDetailRow row = userOrderPersistenceMapper.findOrderByIdAndUserId(id, authenticatedUser.getUserId());
        userOrderPolicy.ensureOrderExists(row != null);
        UserOrderStatus currentStatus = UserOrderStatus.fromLegacy(row.getStatus());
        userOrderPolicy.ensureCancelableStatus(currentStatus);
        String cancelReason = userOrderPolicy.normalizeCancelReason(request == null ? null : request.getCancelReason());

        LocalDateTime now = LocalDateTime.now();
        List<UserOrderItemRow> items = userOrderPersistenceMapper.listOrderItemsByOrderId(id);
        for (UserOrderItemRow item : items) {
            if (item.getBatchId() == null) {
                continue;
            }
            int affectedRows = userOrderPersistenceMapper.decrementBatchLockedQuantity(item.getBatchId(), item.getNumber(), now);
            userOrderPolicy.ensureStockReleased(affectedRows > 0);
        }

        int affectedRows = userOrderPersistenceMapper.cancelOrder(
                id,
                UserOrderStatus.CANCELLED.legacyValue(),
                cancelReason,
                now
        );
        if (affectedRows <= 0) {
            throw new ApplicationException(com.shelfflow.services.common.api.ErrorCode.CONFLICT, "当前订单状态不允许取消");
        }
        insertAndPublishOrderEvent(buildOrderEvent(
                id,
                OrderEventType.CANCELLED,
                OrderEventActorType.USER,
                authenticatedUser.getUserId(),
                currentStatus,
                UserOrderStatus.CANCELLED,
                UserOrderPayStatus.fromLegacy(row.getPayStatus()),
                UserOrderPayStatus.fromLegacy(row.getPayStatus()),
                cancelReason,
                now
        ), row.getNumber(), authenticatedUser.getUserId(), row.getAmount(), items.stream().mapToInt(UserOrderItemRow::getNumber).sum());
    }

    @Transactional
    public UserOrderDetailResponse payOrder(UserAuthenticatedUser authenticatedUser, String orderId) {
        Long id = userOrderPolicy.parseRequiredOrderId(orderId);
        UserOrderDetailRow row = userOrderPersistenceMapper.findOrderByIdAndUserId(id, authenticatedUser.getUserId());
        userOrderPolicy.ensureOrderExists(row != null);
        UserOrderStatus currentStatus = UserOrderStatus.fromLegacy(row.getStatus());
        UserOrderPayStatus currentPayStatus = UserOrderPayStatus.fromLegacy(row.getPayStatus());

        LocalDateTime checkoutTime = LocalDateTime.now();
        UserOrderPaymentDataObject payment = resolveOrCreatePayment(row, authenticatedUser, checkoutTime);
        if (currentPayStatus == UserOrderPayStatus.PAID) {
            markPaymentSucceededIfNecessary(payment, checkoutTime);
            return getOrderDetail(authenticatedUser, orderId);
        }

        userOrderPolicy.ensurePayableStatus(
                currentStatus,
                currentPayStatus
        );

        int affectedRows = userOrderPersistenceMapper.payOrder(
                id,
                UserOrderStatus.TO_PREPARE.legacyValue(),
                UserOrderPayStatus.PAID.legacyValue(),
                checkoutTime
        );
        if (affectedRows <= 0) {
            UserOrderDetailRow latestRow = userOrderPersistenceMapper.findOrderByIdAndUserId(id, authenticatedUser.getUserId());
            if (latestRow != null && UserOrderPayStatus.fromLegacy(latestRow.getPayStatus()) == UserOrderPayStatus.PAID) {
                markPaymentSucceededIfNecessary(payment, checkoutTime);
                return getOrderDetail(authenticatedUser, orderId);
            }
            throw new ApplicationException(com.shelfflow.services.common.api.ErrorCode.CONFLICT, "当前订单状态不允许支付");
        }
        markPaymentSucceededIfNecessary(payment, checkoutTime);
        insertAndPublishOrderEvent(buildOrderEvent(
                id,
                OrderEventType.PAID,
                OrderEventActorType.USER,
                authenticatedUser.getUserId(),
                currentStatus,
                UserOrderStatus.TO_PREPARE,
                currentPayStatus,
                UserOrderPayStatus.PAID,
                "用户确认支付",
                checkoutTime
        ), row.getNumber(), authenticatedUser.getUserId(), row.getAmount(), null);
        return getOrderDetail(authenticatedUser, orderId);
    }

    private UserOrderPaymentDataObject resolveOrCreatePayment(UserOrderDetailRow row,
                                                              UserAuthenticatedUser authenticatedUser,
                                                              LocalDateTime requestTime) {
        UserOrderPaymentDataObject existingPayment = userOrderPersistenceMapper.findOrderPaymentByOrderId(row.getId());
        if (existingPayment != null) {
            return existingPayment;
        }

        UserOrderPaymentDataObject payment = buildOrderPayment(row, authenticatedUser, requestTime);
        try {
            userOrderPersistenceMapper.insertOrderPayment(payment);
            return payment;
        } catch (DuplicateKeyException ex) {
            UserOrderPaymentDataObject concurrentPayment = userOrderPersistenceMapper.findOrderPaymentByOrderId(row.getId());
            if (concurrentPayment != null) {
                return concurrentPayment;
            }
            throw ex;
        }
    }

    private void markPaymentSucceededIfNecessary(UserOrderPaymentDataObject payment, LocalDateTime paidTime) {
        if (payment == null || payment.getId() == null) {
            throw new ApplicationException(com.shelfflow.services.common.api.ErrorCode.CONFLICT, "支付记录不存在");
        }
        if (UserOrderPaymentStatus.fromLegacy(payment.getStatus()) == UserOrderPaymentStatus.SUCCEEDED) {
            return;
        }
        int affectedRows = userOrderPersistenceMapper.markOrderPaymentSucceeded(
                payment.getId(),
                UserOrderPaymentStatus.SUCCEEDED.legacyValue(),
                paidTime,
                paidTime
        );
        if (affectedRows <= 0) {
            UserOrderPaymentDataObject latestPayment = userOrderPersistenceMapper.findOrderPaymentByOrderId(payment.getOrderId());
            if (latestPayment == null
                    || UserOrderPaymentStatus.fromLegacy(latestPayment.getStatus()) != UserOrderPaymentStatus.SUCCEEDED) {
                throw new ApplicationException(com.shelfflow.services.common.api.ErrorCode.CONFLICT, "支付状态更新失败，请刷新后重试");
            }
        }
    }

    private UserOrderPaymentDataObject buildOrderPayment(UserOrderDetailRow row,
                                                         UserAuthenticatedUser authenticatedUser,
                                                         LocalDateTime requestTime) {
        UserOrderPaymentDataObject payment = new UserOrderPaymentDataObject();
        payment.setPaymentNo(buildPaymentNo(row));
        payment.setOrderId(row.getId());
        payment.setOrderNumber(row.getNumber());
        payment.setUserId(authenticatedUser.getUserId());
        payment.setAmount(row.getAmount());
        payment.setPayMethod(row.getPayMethod() == null ? userOrderProperties.getDefaultPayMethod() : row.getPayMethod());
        payment.setProvider(DEFAULT_PAYMENT_PROVIDER);
        payment.setStatus(UserOrderPaymentStatus.PENDING.legacyValue());
        payment.setIdempotencyKey(buildPaymentIdempotencyKey(authenticatedUser.getUserId(), row.getId()));
        payment.setRequestTime(requestTime);
        payment.setPaidTime(null);
        payment.setCreateTime(requestTime);
        payment.setUpdateTime(requestTime);
        return payment;
    }

    private String buildPaymentNo(UserOrderDetailRow row) {
        String orderNumber = row.getNumber() == null ? String.valueOf(row.getId()) : row.getNumber();
        return PAYMENT_NO_PREFIX + orderNumber;
    }

    private String buildPaymentIdempotencyKey(Long userId, Long orderId) {
        return PAYMENT_IDEMPOTENCY_PREFIX
                + PAYMENT_IDEMPOTENCY_SEPARATOR
                + userId
                + PAYMENT_IDEMPOTENCY_SEPARATOR
                + orderId;
    }

    private void insertAndPublishOrderEvent(UserOrderEventDataObject event,
                                            String orderNumber,
                                            Long userId,
                                            BigDecimal totalAmount,
                                            Integer itemCount) {
        userOrderPersistenceMapper.insertOrderEvent(event);
        userOrderEventPublisher.publishAfterCommit(UserOrderEventMessage.from(
                event,
                orderNumber,
                userId,
                totalAmount,
                itemCount
        ));
    }

    private UserOrderEventDataObject buildOrderEvent(Long orderId,
                                                     OrderEventType eventType,
                                                     OrderEventActorType actorType,
                                                     Long actorId,
                                                     UserOrderStatus fromStatus,
                                                     UserOrderStatus toStatus,
                                                     UserOrderPayStatus fromPayStatus,
                                                     UserOrderPayStatus toPayStatus,
                                                     String note,
                                                     LocalDateTime eventTime) {
        UserOrderEventDataObject event = new UserOrderEventDataObject();
        event.setOrderId(orderId);
        event.setEventType(eventType.value());
        event.setActorType(actorType.value());
        event.setActorId(actorId);
        event.setFromStatus(fromStatus == null ? null : fromStatus.legacyValue());
        event.setToStatus(toStatus == null ? null : toStatus.legacyValue());
        event.setFromPayStatus(fromPayStatus == null ? null : fromPayStatus.legacyValue());
        event.setToPayStatus(toPayStatus == null ? null : toPayStatus.legacyValue());
        event.setNote(note);
        event.setCreateTime(eventTime);
        return event;
    }

    private UserOrderDataObject buildOrder(UserAuthenticatedUser authenticatedUser,
                                           UserOrderSubmitRequest request,
                                           List<UserOrderCartItemRow> cartItems,
                                           UserAccountDataObject user,
                                           UserPickupContactDataObject pickupContact,
                                           UserPickupPointDataObject pickupPoint,
                                           LocalDateTime now) {
        UserOrderDataObject order = new UserOrderDataObject();
        order.setNumber(userOrderPolicy.nextOrderNumber(now));
        order.setStatus(UserOrderStatus.PENDING_PAYMENT.legacyValue());
        order.setUserId(authenticatedUser.getUserId());
        order.setPickupContactId(pickupContact == null ? null : pickupContact.getId());
        order.setOrderTime(now);
        order.setCheckoutTime(null);
        order.setPayMethod(userOrderProperties.getDefaultPayMethod());
        order.setPayStatus(UserOrderPayStatus.UNPAID.legacyValue());
        order.setAmount(userOrderPolicy.calculateOrderAmount(cartItems));
        order.setRemark(userOrderPolicy.normalizeOptionalRemark(request.getRemark()));
        order.setPhone(resolveOrderPhone(user, pickupContact));
        order.setUserName(user == null ? null : user.getName());
        order.setConsignee(resolveOrderConsignee(user, pickupContact));
        order.setPickupPoint(userOrderPolicy.resolvePickupPoint(pickupPoint));
        order.setPreparationMode(userOrderProperties.getDefaultPreparationMode());
        order.setFulfillmentFee(userOrderProperties.getDefaultFulfillmentFee());
        order.setPackageCount(userOrderProperties.getDefaultPackageCount());
        order.setPackageStrategy(userOrderProperties.getDefaultPackageStrategy());
        order.setFulfillmentType(userOrderProperties.getDefaultFulfillmentType());
        order.setPickupCode(userOrderPolicy.nextPickupCode());
        order.setPickupDeadline(userOrderPolicy.resolvePickupDeadline(now));
        return order;
    }

    private UserPickupContactDataObject resolvePickupContact(UserAuthenticatedUser authenticatedUser, UserOrderSubmitRequest request) {
        if (request.getPickupContactId() != null && !request.getPickupContactId().isBlank()) {
            Long contactId = userPickupContactPolicy.parseRequiredContactId(request.getPickupContactId());
            UserPickupContactDataObject contact = userPickupContactPersistenceMapper.findByIdAndUserId(contactId, authenticatedUser.getUserId());
            userPickupContactPolicy.ensureContactExists(contact != null);
            return contact;
        }

        return userPickupContactPersistenceMapper.listByUserId(authenticatedUser.getUserId()).stream()
                .filter(contact -> Integer.valueOf(1).equals(contact.getIsDefault()))
                .findFirst()
                .orElse(null);
    }

    private UserPickupPointDataObject resolvePickupPoint(UserOrderSubmitRequest request) {
        Long pickupPointId = userOrderPolicy.parseOptionalPickupPointId(request.getPickupPointId());
        if (pickupPointId == null) {
            return userPickupPointPersistenceMapper.listEnabled().stream().findFirst().orElse(null);
        }
        UserPickupPointDataObject pickupPoint = userPickupPointPersistenceMapper.findEnabledById(pickupPointId);
        userOrderPolicy.ensurePickupPointExists(pickupPoint != null);
        return pickupPoint;
    }

    private String resolveOrderPhone(UserAccountDataObject user, UserPickupContactDataObject pickupContact) {
        if (pickupContact != null && pickupContact.getPhone() != null && !pickupContact.getPhone().isBlank()) {
            return pickupContact.getPhone();
        }
        return user == null ? null : user.getPhone();
    }

    private String resolveOrderConsignee(UserAccountDataObject user, UserPickupContactDataObject pickupContact) {
        if (pickupContact != null && pickupContact.getConsignee() != null && !pickupContact.getConsignee().isBlank()) {
            return pickupContact.getConsignee();
        }
        return user == null ? null : user.getName();
    }

    private List<UserOrderDetailDataObject> buildOrderDetails(Long orderId, List<UserOrderCartItemRow> cartItems) {
        List<UserOrderDetailDataObject> items = new ArrayList<>(cartItems.size());
        for (UserOrderCartItemRow cartItem : cartItems) {
            UserOrderDetailDataObject item = new UserOrderDetailDataObject();
            item.setOrderId(orderId);
            item.setProductId(cartItem.getProductId());
            item.setBatchId(cartItem.getBatchId());
            item.setName(cartItem.getName());
            item.setImage(cartItem.getImage());
            item.setProductSpec(cartItem.getProductSpec());
            item.setNumber(cartItem.getQuantity());
            item.setAmount(cartItem.getAmount());
            items.add(item);
        }
        return items;
    }

    private Map<Long, List<UserOrderItemResponse>> resolveOrderItems(List<UserOrderSummaryRow> rows) {
        if (rows.isEmpty()) {
            return Map.of();
        }
        List<Long> orderIds = rows.stream().map(UserOrderSummaryRow::getId).toList();
        List<UserOrderItemRow> itemRows = userOrderPersistenceMapper.listOrderItemsByOrderIds(orderIds);
        Map<Long, List<UserOrderItemResponse>> result = new LinkedHashMap<>();
        for (UserOrderItemRow itemRow : itemRows) {
            result.computeIfAbsent(itemRow.getOrderId(), ignored -> new ArrayList<>())
                    .add(toItemResponse(itemRow));
        }
        return result;
    }

    private UserOrderItemResponse toItemResponse(UserOrderItemRow itemRow) {
        BigDecimal lineAmount = itemRow.getAmount().multiply(BigDecimal.valueOf(itemRow.getNumber()));
        return UserOrderItemResponse.builder()
                .productId(String.valueOf(itemRow.getProductId()))
                .batchId(itemRow.getBatchId() == null ? null : String.valueOf(itemRow.getBatchId()))
                .name(itemRow.getName())
                .image(itemRow.getImage())
                .productSpec(itemRow.getProductSpec())
                .quantity(itemRow.getNumber())
                .unitPrice(itemRow.getAmount())
                .lineAmount(lineAmount)
                .build();
    }

    private OrderEventResponse toEventResponse(UserOrderEventDataObject row) {
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
