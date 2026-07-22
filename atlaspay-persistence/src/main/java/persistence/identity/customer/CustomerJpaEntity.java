package persistence.identity.customer;

import core.identity.valueobject.KycStatus;
import core.identity.valueobject.KycTier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerJpaEntity {
    @Id
    private UUID id;

    private UUID companyId;

    private String fullName;

    private LocalDate dateOfBirth;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String postalCode;

    private String countryCode;

    @Enumerated(EnumType.STRING)
    private KycTier kycTier;

    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus;

    @Version
    private long version;

    public CustomerJpaEntity(UUID id, UUID companyId, String fullName, LocalDate dateOfBirth, String addressLine1, String addressLine2, String city, String postalCode, String countryCode, KycTier kycTier, KycStatus kycStatus) {
        this.id = id;
        this.companyId = companyId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
        this.kycTier = kycTier;
        this.kycStatus = kycStatus;
    }
}
