package com.shelfflow.services.user.order.service;

import com.shelfflow.services.common.domain.OrderEventActorType;
import com.shelfflow.services.common.domain.OrderEventType;
import com.shelfflow.services.common.domain.UserOrderPayStatus;
import com.shelfflow.services.common.domain.UserOrderStatus;
import com.shelfflow.services.user.config.UserOrderProperties;
import com.shelfflow.services.user.order.messaging.UserOrderEventMessage;
import com.shelfflow.services.user.order.messaging.UserOrderEventPublisher;
import com.shelfflow.services.user.order.persistence.UserOrderPersistenceMapper;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderEventDataObject;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderItemRow;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderTimeoutCandidateRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserOrderTimeoutCloseService {

    private static final long MIN_TIMEOUT_MINUTES = 1L;
    private static final int MIN_BATCH_SIZE = 1;

    private final UserOrderPersistenceMapper userOrderPersistenceMapper;
    private final UserOrderProperties userOrderProperties;
    private final UserOrderEventPublisher userOrderEventPublisher;
    private final TransactionTemplate transactionTemplate;

    public UserOrderTimeoutCloseService(UserOrderPersistenceMapper userOrderPersistenceMapper,
                                        UserOrderProperties userOrderProperties,
                                        UserOrderEventPublisher userOrderEventPublisher,
                                        TransactionTemplate transactionTemplate) {
        this.userOrderPersistenceMapper = userOrderPersistenceMapper;
        this.userOrderProperties = userOrderProperties;
        this.userOrderEventPublisher = userOrderEventPublisher;
        this.transactionTemplate = transactionTemplate;
    }

    public int closeExpiredPendingPaymentOrders(LocalDateTime currentTime) {
        LocalDateTime effectiveCurrentTime = currentTime == null ? LocalDateTime.now() : currentTime;
        LocalDateTime deadline = effectiveCurrentTime.minusMinutes(resolveUnpaidTimeoutMinutes());
        List<UserOrderTimeoutCandidateRow> candidates = userOrderPersistenceMapper.listTimeoutCloseCandidates(
                deadline,
                UserOrderStatus.PENDING_PAYMENT.legacyValue(),
                UserOrderPayStatus.UNPAID.legacyValue(),
                resolveBatchSize()
        );

        int closedCount = 0;
        for (UserOrderTimeoutCandidateRow candidate : candidates) {
            try {
                Boolean closed = transactionTemplate.execute(status -> closeOne(candidate, effectiveCurrentTime, deadline));
                if (Boolean.TRUE.equals(closed)) {
                    closedCount++;
                }
            } catch (RuntimeException ex) {
                log.error("Timeout close order failed. orderId={}", candidate == null ? null : candidate.getId(), ex);
            }
        }
        return closedCount;
    }

    private boolean closeOne(UserOrderTimeoutCandidateRow candidate, LocalDateTime cancelTime, LocalDateTime deadline) {
        if (candidate == null || candidate.getId() == null) {
            return false;
        }

        String cancelReason = resolveCancelReason();
        int affectedRows = userOrderPersistenceMapper.timeoutCancelOrder(
                candidate.getId(),
                UserOrderStatus.PENDING_PAYMENT.legacyValue(),
                UserOrderPayStatus.UNPAID.legacyValue(),
                UserOrderStatus.CANCELLED.legacyValue(),
                cancelReason,
                cancelTime,
                deadline
        );
        if (affectedRows <= 0) {
            return false;
        }

        List<UserOrderItemRow> items = userOrderPersistenceMapper.listOrderItemsByOrderId(candidate.getId());
        for (UserOrderItemRow item : items) {
            if (item.getBatchId() == null || item.getNumber() == null || item.getNumber() <= 0) {
                continue;
            }
            int releasedRows = userOrderPersistenceMapper.decrementBatchLockedQuantity(
                    item.getBatchId(),
                    item.getNumber(),
                    cancelTime
            );
            if (releasedRows <= 0) {
                throw new IllegalStateException("Failed to release locked stock for timeout closed order: " + candidate.getId());
            }
        }

        UserOrderEventDataObject event = buildTimeoutCancelEvent(candidate, cancelReason, cancelTime);
        userOrderPersistenceMapper.insertOrderEvent(event);
        userOrderEventPublisher.publishAfterCommit(UserOrderEventMessage.from(
                event,
                candidate.getNumber(),
                candidate.getUserId(),
                candidate.getAmount(),
                items.stream().mapToInt(item -> item.getNumber() == null ? 0 : item.getNumber()).sum()
        ));
        return true;
    }

    private UserOrderEventDataObject buildTimeoutCancelEvent(UserOrderTimeoutCandidateRow candidate,
                                                            String cancelReason,
                                                            LocalDateTime cancelTime) {
        UserOrderEventDataObject event = new UserOrderEventDataObject();
        event.setOrderId(candidate.getId());
        event.setEventType(OrderEventType.CANCELLED.value());
        event.setActorType(OrderEventActorType.SYSTEM.value());
        event.setActorId(null);
        event.setFromStatus(UserOrderStatus.PENDING_PAYMENT.legacyValue());
        event.setToStatus(UserOrderStatus.CANCELLED.legacyValue());
        event.setFromPayStatus(UserOrderPayStatus.UNPAID.legacyValue());
        event.setToPayStatus(UserOrderPayStatus.UNPAID.legacyValue());
        event.setNote(cancelReason);
        event.setCreateTime(cancelTime);
        return event;
    }

    private long resolveUnpaidTimeoutMinutes() {
        return Math.max(userOrderProperties.getTimeoutClose().getUnpaidTimeoutMinutes(), MIN_TIMEOUT_MINUTES);
    }

    private int resolveBatchSize() {
        return Math.max(userOrderProperties.getTimeoutClose().getBatchSize(), MIN_BATCH_SIZE);
    }

    private String resolveCancelReason() {
        String configuredReason = userOrderProperties.getTimeoutClose().getCancelReason();
        return StringUtils.hasText(configuredReason)
                ? configuredReason.trim()
                : "订单超时未支付，系统自动取消";
    }
}
