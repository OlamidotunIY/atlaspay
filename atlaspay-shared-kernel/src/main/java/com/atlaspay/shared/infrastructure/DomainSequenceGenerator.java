package com.atlaspay.shared.infrastructure;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DomainSequenceGenerator {

    private final JdbcTemplate jdbcTemplate;

    public DomainSequenceGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long nextIdentity(String sequenceName) {
        String selectSql = "SELECT next_val FROM domain_sequences WHERE sequence_name = ? FOR UPDATE";
        Long currentVal = jdbcTemplate.queryForObject(selectSql, Long.class, sequenceName);
        
        if (currentVal == null) {
            throw new IllegalStateException("Sequence not found: " + sequenceName);
        }

        String updateSql = "UPDATE domain_sequences SET next_val = next_val + 1 WHERE sequence_name = ?";
        jdbcTemplate.update(updateSql, sequenceName);

        return currentVal;
    }
}
