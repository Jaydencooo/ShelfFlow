package com.shelfflow.services.user.order.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.user.order.persistence.UserOrderEventOutboxPersistenceMapper;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderEventOutboxDataObject;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserOrderEventOutboxApplicationService {

    private static final int DEFAULT_ATTEMPT_COUNT = 0;
    private static final int MAX_LAST_ERROR_LENGTH = 512;

    private final ObjectMapper objectMapper;
    private final UserOrderEventOutboxPersistenceMapper outboxPersistenceMapper;

    public UserOrderEventOutboxApplicationService(ObjectMapper objectMapper,
                                                  UserOrderEventOutboxPersistenceMapper outboxPersistenceMapper) {
        this.objectMapper = objectMapper;
        this.outboxPersistenceMapper = outboxPersistenceMapper;
    }

    @Transactional
    public void enqueue(UserOrderEventMessage eventMessage,
                        String messageId,
                        String exchange,
                        String routingKey,
                        LocalDateTime now) {
        try {
            UserOrderEventOutboxDataObject outbox = new UserOrderEventOutboxDataObject();
            outbox.setEventId(eventMessage.getEventId());
            outbox.setMessageId(messageId);
            outbox.setExchangeName(exchange);
            outbox.setRoutingKey(routingKey);
            outbox.setPayloadJson(objectMapper.writeValueAsString(eventMessage));
            outbox.setStatus(UserOrderEventOutboxStatus.PENDING.name());
            outbox.setAttemptCount(DEFAULT_ATTEMPT_COUNT);
            outbox.setNextRetryTime(now);
            outbox.setCreateTime(now);
            outbox.setUpdateTime(now);
            outboxPersistenceMapper.insert(outbox);
        } catch (DuplicateKeyException ignored) {
            // The event log id is the idempotency key; duplicate enqueue means a previous transaction already recorded it.
        } catch (JsonProcessingException ex) {
            throw new ApplicationException(ErrorCode.INTERNAL_ERROR, "订单事件序列化失败");
        }
    }

    @Transactional(readOnly = true)
    public List<UserOrderEventOutboxDataObject> listPendingDue(LocalDateTime now, int limit, int maxAttempts) {
        return outboxPersistenceMapper.listPendingDue(now, limit, maxAttempts);
    }

    @Transactional
    public void markPublished(Long id, LocalDateTime publishedTime) {
        outboxPersistenceMapper.markPublished(id, publishedTime, publishedTime);
    }

    @Transactional
    public void markFailed(Long id, LocalDateTime nextRetryTime, String lastError, LocalDateTime updateTime) {
        outboxPersistenceMapper.markFailed(id, nextRetryTime, truncate(lastError), updateTime);
    }

    public UserOrderEventMessage deserialize(UserOrderEventOutboxDataObject outbox) {
        try {
            return objectMapper.readValue(outbox.getPayloadJson(), UserOrderEventMessage.class);
        } catch (JsonProcessingException ex) {
            throw new ApplicationException(ErrorCode.INTERNAL_ERROR, "订单事件反序列化失败");
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= MAX_LAST_ERROR_LENGTH ? value : value.substring(0, MAX_LAST_ERROR_LENGTH);
    }
}
