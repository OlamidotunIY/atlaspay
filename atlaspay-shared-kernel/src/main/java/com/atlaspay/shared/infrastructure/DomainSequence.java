package com.atlaspay.shared.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "domain_sequences")
public class DomainSequence {

    @Id
    @Column(name = "sequence_name", nullable = false)
    private String sequenceName;

    @Setter
    @Column(name = "next_val", nullable = false)
    private Long nextVal;

    protected DomainSequence() {
        // JPA
    }

    public DomainSequence(String sequenceName, Long nextVal) {
        this.sequenceName = sequenceName;
        this.nextVal = nextVal;
    }

}
