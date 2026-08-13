package com.atlaspay.shared.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "domain_sequences")
public class DomainSequence {

    @Id
    @Column(name = "sequence_name", nullable = false)
    private String sequenceName;

    @Column(name = "next_val", nullable = false)
    private Long nextVal;

    protected DomainSequence() {
        // JPA
    }

    public DomainSequence(String sequenceName, Long nextVal) {
        this.sequenceName = sequenceName;
        this.nextVal = nextVal;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public Long getNextVal() {
        return nextVal;
    }

    public void setNextVal(Long nextVal) {
        this.nextVal = nextVal;
    }
}
