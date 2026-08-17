package com.atlaspay.ledger.infrastructure.adapter.persistence;

import com.atlaspay.ledger.domain.model.BalanceSnapshot;
import com.atlaspay.ledger.domain.repository.BalanceSnapshotRepository;
import com.atlaspay.ledger.infrastructure.entity.BalanceSnapshotJpaEntity;
import com.atlaspay.ledger.domain.repository.BalanceSnapshotRepository;
import com.atlaspay.ledger.infrastructure.entity.BalanceSnapshotJpaEntity;
import com.atlaspay.ledger.infrastructure.mapper.BalanceSnapshotMapper;
import com.atlaspay.ledger.infrastructure.repository.SpringDataBalanceSnapshotRepository;
import com.atlaspay.shared.infrastructure.DomainSequenceGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class BalanceSnapshotRepositoryAdapter implements BalanceSnapshotRepository {

    private final SpringDataBalanceSnapshotRepository jpaRepository;
    private final DomainSequenceGenerator sequenceGenerator;
    private final BalanceSnapshotMapper mapper;

    public BalanceSnapshotRepositoryAdapter(SpringDataBalanceSnapshotRepository jpaRepository, DomainSequenceGenerator sequenceGenerator, BalanceSnapshotMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("balance_snapshot_seq");
    }

    @Override
    public Optional<BalanceSnapshot> findLatestByAccountId(Long accountId) {
        return jpaRepository.findLatestSnapshot(accountId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<BalanceSnapshot> findLatestByAccountIdForUpdate(Long accountId) {
        return jpaRepository.findLatestSnapshotForUpdate(accountId)
                .map(mapper::toDomain);
    }

    @Override
    public BalanceSnapshot save(BalanceSnapshot snapshot) {
        BalanceSnapshotJpaEntity entity = mapper.toEntity(snapshot);
        jpaRepository.save(entity);
        return snapshot;
    }


}
