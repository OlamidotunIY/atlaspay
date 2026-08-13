package com.atlaspay.eventbus.infrastructure.adapter;

import com.atlaspay.eventbus.domain.model.OutboxStatus;
import com.atlaspay.eventbus.infrastructure.entity.OutboxMessageJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataOutboxMessageRepository extends JpaRepository<OutboxMessageJpaEntity, String> {

    @Query("SELECT o FROM OutboxMessageJpaEntity o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<OutboxMessageJpaEntity> findByStatusOrderByCreatedAtAsc(@org.springframework.data.repository.query.Param("status") OutboxStatus status, Pageable pageable);
}
