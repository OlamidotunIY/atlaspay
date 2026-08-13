package com.atlaspay.simulator.infrastructure.adapter;

import com.atlaspay.simulator.domain.repository.SimulatorAccountRepository;
import com.atlaspay.simulator.infrastructure.persistence.JpaSimulatorAccountRepository;
import com.atlaspay.simulator.infrastructure.persistence.SimulatorAccountJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class SimulatorAccountPersistenceAdapter implements SimulatorAccountRepository {

    private final JpaSimulatorAccountRepository jpaRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long getNextAccountSerial() {
        // MySQL 8 does not support CREATE SEQUENCE.
        // We simulate a sequence using a dedicated table with an AUTO_INCREMENT column.
        jdbcTemplate.update("INSERT INTO account_serial_seq VALUES ()");
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    @Override
    @Transactional
    public void saveAccount(String id, String reference, String bankName, String bankCode, long accountSerial, String nuban, String accountName, String callbackUrl, String status) {
        SimulatorAccountJpaEntity entity = new SimulatorAccountJpaEntity(
                id, reference, bankName, bankCode, accountSerial, nuban, accountName, callbackUrl, status, ZonedDateTime.now(ZoneOffset.UTC)
        );
        
        jpaRepository.save(entity);
    }
}
