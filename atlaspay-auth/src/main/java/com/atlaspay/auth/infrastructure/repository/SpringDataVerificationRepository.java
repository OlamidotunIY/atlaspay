package com.atlaspay.auth.infrastructure.repository;

import com.atlaspay.auth.domain.model.VerificationStatus;
import com.atlaspay.auth.domain.model.VerificationType;
import com.atlaspay.auth.infrastructure.entity.VerificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataVerificationRepository extends JpaRepository<VerificationJpaEntity, Long> {
    Optional<VerificationJpaEntity> findByTypeAndValueAndStatus(VerificationType type, String value, VerificationStatus status);
    List<VerificationJpaEntity> findByStatus(VerificationStatus status);

    @Modifying
    @Query("UPDATE VerificationJpaEntity v SET v.status = 'EXPIRED' WHERE v.type = :type AND v.value = :value AND v.status = 'PENDING'")
    void invalidatePreviousVerifications(@Param("type") VerificationType type, @Param("value") String value);
}
