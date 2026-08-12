package com.atlaspay.eventbus.domain.repository;

import com.atlaspay.eventbus.domain.model.OutboxMessage;
import com.atlaspay.eventbus.domain.model.OutboxStatus;
import java.util.List;

public interface OutboxMessageRepository {
    void save(OutboxMessage message);
    List<OutboxMessage> findPendingMessagesBatch(int batchSize);
    void saveAll(List<OutboxMessage> messages);
}
