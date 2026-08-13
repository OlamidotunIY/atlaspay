package com.atlaspay.eventbus.infrastructure.adapter;

import com.atlaspay.eventbus.domain.model.OutboxMessage;
import com.atlaspay.eventbus.domain.model.OutboxStatus;
import com.atlaspay.eventbus.domain.repository.OutboxMessageRepository;
import com.atlaspay.eventbus.infrastructure.entity.OutboxMessageJpaEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JpaOutboxMessageRepositoryAdapter implements OutboxMessageRepository {

    private final SpringDataOutboxMessageRepository repository;

    public JpaOutboxMessageRepositoryAdapter(SpringDataOutboxMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(OutboxMessage message) {
        repository.save(mapToEntity(message));
    }

    @Override
    public List<OutboxMessage> findPendingMessagesBatch(int batchSize) {
        return repository.findByStatusOrderByCreatedAtAsc(
            OutboxStatus.PENDING,
            PageRequest.of(0, batchSize)
        ).stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public void saveAll(List<OutboxMessage> messages) {
        repository.saveAll(messages.stream().map(this::mapToEntity).collect(Collectors.toList()));
    }

    private OutboxMessageJpaEntity mapToEntity(OutboxMessage domain) {
        return new OutboxMessageJpaEntity(
                domain.getId(),
                domain.getTopic(),
                domain.getPayload(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getProcessedAt()
        );
    }

    private OutboxMessage mapToDomain(OutboxMessageJpaEntity entity) {
        return new OutboxMessage(
                entity.getId(),
                entity.getTopic(),
                entity.getPayload(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getProcessedAt()
        );
    }
}
