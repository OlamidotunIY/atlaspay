package persistence.accounts;

import core.accounts.valueobjects.AccountStatus;
import core.accounts.valueobjects.AccountType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountJpaEntity {
    @Id
    private UUID id;
    private String accountNumber;
    private UUID ownerId;
    @Enumerated(EnumType.STRING)
    private AccountType type;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    @Version
    private long version;

    public AccountJpaEntity(UUID id, String accountNumber, UUID ownerId, AccountType type, AccountStatus status) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.ownerId = ownerId;
        this.type = type;
        this.status = status;
    }
}
