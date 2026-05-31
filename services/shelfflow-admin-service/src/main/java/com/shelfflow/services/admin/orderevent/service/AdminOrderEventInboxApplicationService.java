package com.shelfflow.services.admin.orderevent.service;

import com.shelfflow.services.admin.orderevent.domain.AdminOrderEventInboxStatus;
import com.shelfflow.services.admin.orderevent.messaging.AdminOrderEventMessage;
import com.shelfflow.services.admin.orderevent.persistence.AdminOrderEventInboxPersistenceMapper;
import com.shelfflow.services.admin.orderevent.persistence.dataobject.AdminOrderEventInboxDataObject;
import com.shelfflow.services.admin.orderevent.persistence.dataobject.AdminOrderEventInboxPageCriteria;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.AdminOrderEventInboxQuery;
import com.shelfflow.services.common.dto.AdminOrderEventInboxResponse;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminOrderEventInboxApplicationService {

    private static final int DEFAULT_RETRY_COUNT = 0;
    private static final int MAX_FAILURE_REASON_LENGTH = 512;

    private final AdminOrderEventInboxPersistenceMapper inboxPersistenceMapper;

    public AdminOrderEventInboxApplicationService(AdminOrderEventInboxPersistenceMapper inboxPersistenceMapper) {
        this.inboxPersistenceMapper = inboxPersistenceMapper;
    }

    @Transactional
    public AdminOrderEventInboxResponse consume(AdminOrderEventMessage message, String messageId, String routingKey) {
        validateMessage(message);
        AdminOrderEventInboxDataObject existing = inboxPersistenceMapper.findByEventId(message.getEventId());
        if (existing != null) {
            return toResponse(existing);
        }

        LocalDateTime now = LocalDateTime.now();
        AdminOrderEventInboxDataObject inbox = buildInbox(message, messageId, routingKey, now);
        try {
            inboxPersistenceMapper.insert(inbox);
        } catch (DuplicateKeyException ex) {
            AdminOrderEventInboxDataObject duplicate = inboxPersistenceMapper.findByEventId(message.getEventId());
            if (duplicate != null) {
                return toResponse(duplicate);
            }
            throw ex;
        }

        inboxPersistenceMapper.markProcessed(inbox.getId(), now, now);
        inbox.setStatus(AdminOrderEventInboxStatus.PROCESSED.name());
        inbox.setProcessedTime(now);
        inbox.setUpdateTime(now);
        return toResponse(inbox);
    }

    @Transactional
    public void markFailed(Long eventId, String failureReason) {
        if (eventId == null) {
            return;
        }
        AdminOrderEventInboxDataObject existing = inboxPersistenceMapper.findByEventId(eventId);
        if (existing == null) {
            return;
        }
        inboxPersistenceMapper.markFailed(existing.getId(), truncate(failureReason), LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminOrderEventInboxResponse> page(AdminOrderEventInboxQuery query) {
        AdminOrderEventInboxPageCriteria criteria = AdminOrderEventInboxPageCriteria.builder()
                .orderNumber(blankToNull(query.getOrderNumber()))
                .eventType(blankToNull(query.getEventType()))
                .status(blankToNull(query.getStatus()))
                .offset((query.getPage() - 1) * query.getPageSize())
                .pageSize(query.getPageSize())
                .build();
        List<AdminOrderEventInboxResponse> items = inboxPersistenceMapper.page(criteria).stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.<AdminOrderEventInboxResponse>builder()
                .items(items)
                .total(inboxPersistenceMapper.count(criteria))
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }

    private void validateMessage(AdminOrderEventMessage message) {
        if (message == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "订单事件不能为空");
        }
        if (message.getEventId() == null || message.getOrderId() == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "订单事件缺少必要标识");
        }
        if (!StringUtils.hasText(message.getEventType())) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "订单事件类型不能为空");
        }
    }

    private AdminOrderEventInboxDataObject buildInbox(AdminOrderEventMessage message,
                                                      String messageId,
                                                      String routingKey,
                                                      LocalDateTime now) {
        AdminOrderEventInboxDataObject inbox = new AdminOrderEventInboxDataObject();
        inbox.setEventId(message.getEventId());
        inbox.setMessageId(blankToNull(messageId));
        inbox.setOrderId(message.getOrderId());
        inbox.setOrderNumber(blankToNull(message.getOrderNumber()));
        inbox.setUserId(message.getUserId());
        inbox.setEventType(message.getEventType().trim());
        inbox.setActorType(blankToNull(message.getActorType()));
        inbox.setActorId(message.getActorId());
        inbox.setFromStatus(message.getFromStatus());
        inbox.setToStatus(message.getToStatus());
        inbox.setFromPayStatus(message.getFromPayStatus());
        inbox.setToPayStatus(message.getToPayStatus());
        inbox.setTotalAmount(message.getTotalAmount());
        inbox.setItemCount(message.getItemCount());
        inbox.setNote(blankToNull(message.getNote()));
        inbox.setRoutingKey(blankToNull(routingKey));
        inbox.setStatus(AdminOrderEventInboxStatus.RECEIVED.name());
        inbox.setRetryCount(DEFAULT_RETRY_COUNT);
        inbox.setEventTime(message.getEventTime());
        inbox.setReceivedTime(now);
        inbox.setCreateTime(now);
        inbox.setUpdateTime(now);
        return inbox;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= MAX_FAILURE_REASON_LENGTH ? value : value.substring(0, MAX_FAILURE_REASON_LENGTH);
    }

    private AdminOrderEventInboxResponse toResponse(AdminOrderEventInboxDataObject row) {
        return AdminOrderEventInboxResponse.builder()
                .id(String.valueOf(row.getId()))
                .eventId(row.getEventId())
                .messageId(row.getMessageId())
                .orderId(row.getOrderId())
                .orderNumber(row.getOrderNumber())
                .userId(row.getUserId())
                .eventType(row.getEventType())
                .actorType(row.getActorType())
                .actorId(row.getActorId())
                .fromStatus(row.getFromStatus())
                .toStatus(row.getToStatus())
                .fromPayStatus(row.getFromPayStatus())
                .toPayStatus(row.getToPayStatus())
                .totalAmount(row.getTotalAmount())
                .itemCount(row.getItemCount())
                .note(row.getNote())
                .routingKey(row.getRoutingKey())
                .status(row.getStatus())
                .retryCount(row.getRetryCount())
                .failureReason(row.getFailureReason())
                .eventTime(row.getEventTime())
                .receivedTime(row.getReceivedTime())
                .processedTime(row.getProcessedTime())
                .build();
    }
}
