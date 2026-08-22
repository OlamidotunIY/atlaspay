package com.atlaspay.auth.infrastructure.adapter.persistence;

import com.atlaspay.auth.domain.model.Verification;
import com.atlaspay.auth.domain.model.VerificationStatus;
import com.atlaspay.auth.domain.model.VerificationType;
import com.atlaspay.auth.domain.repository.VerificationRepository;
import com.atlaspay.auth.infrastructure.entity.VerificationJpaEntity;
import com.atlaspay.auth.infrastructure.mapper.VerificationMapper;
import com.atlaspay.auth.infrastructure.repository.SpringDataVerificationRepository;
import com.atlaspay.shared.infrastructure.DomainSequenceGenerator;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class VerificationRepositoryAdapter implements VerificationRepository {

    private final SpringDataVerificationRepository jpaRepository;
    private final DomainSequenceGenerator sequenceGenerator;
    private final VerificationMapper mapper;

    public VerificationRepositoryAdapter(SpringDataVerificationRepository jpaRepository, DomainSequenceGenerator sequenceGenerator, VerificationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("verification_seq");
    }

    @Override
    public Verification save(Verification verification) {
        VerificationJpaEntity entity = mapper.toEntity(verification);
        jpaRepository.save(entity);
        return verification;
    }

    @Override
    public Optional<Verification> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Verification> findActiveByTypeAndValue(VerificationType type, String value) {
        return jpaRepository.findByTypeAndValueAndStatus(type, value, VerificationStatus.PENDING)
                .map(mapper::toDomain);
    }

    @Override
    public List<Verification> findByStatus(VerificationStatus status) {
        return jpaRepository.findByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void invalidatePreviousVerifications(VerificationType type, String value) {
        jpaRepository.invalidatePreviousVerifications(type, value);
    }
}
