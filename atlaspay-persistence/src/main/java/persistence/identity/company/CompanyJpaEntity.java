package persistence.identity.company;

import core.identity.valueobject.CompanyStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyJpaEntity {
    @Id
    private UUID id;

    private String name;

    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    private CompanyStatus status;

    private Instant onboardedAt;

    @Version
    private long version;

    public CompanyJpaEntity(
            UUID id,
            String name,
            String registrationNumber,
            CompanyStatus status,
            Instant onboardedAt
    ) {
        this.id = id;
        this.name = name;
        this.registrationNumber = registrationNumber;
        this.status = status;
        this.onboardedAt = onboardedAt;
    }
}
