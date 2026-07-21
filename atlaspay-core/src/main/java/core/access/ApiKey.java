package core.access;

import core.access.events.ApiKeyIssued;
import core.access.events.ApiKeyRevoked;
import core.access.valueobjects.ApiKeyId;
import core.access.valueobjects.ApiKeyStatus;
import core.access.valueobjects.HashedSecret;
import core.access.valueobjects.Scope;
import core.identity.valueobject.CompanyId;
import core.shared.AggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ApiKey extends AggregateRoot<ApiKeyId> {
    private final CompanyId ownerCompanyId;
    private final HashedSecret hashedSecret;          // never stores plaintext secret
    private final Set<Scope> scopes;                    // unmodifiable
    private ApiKeyStatus status;                          // ACTIVE, REVOKED

    public ApiKey(ApiKeyId apiKeyId, CompanyId ownerCompanyId, HashedSecret hashedSecret, Set<Scope> scopes) {
        super(apiKeyId);
        if (scopes == null) {
            throw new IllegalArgumentException("Scopes cannot be null");
        }
        this.ownerCompanyId = ownerCompanyId;
        this.hashedSecret = hashedSecret;
        this.scopes = scopes;
        this.status = ApiKeyStatus.ACTIVE;
        register(new ApiKeyIssued(UUID.randomUUID(), Instant.now(), apiKeyId, ownerCompanyId));
    }

    public void revoke() {
        if (this.status == ApiKeyStatus.REVOKED) {
            throw new IllegalStateException("API key is already revoked");
        }
        this.status = ApiKeyStatus.REVOKED;
        register(new ApiKeyRevoked(UUID.randomUUID(), Instant.now(), id()));
    }

    public boolean hasScope(Scope scope) {
        Objects.requireNonNull(scope, "Scope cannot be null");
        return scopes.contains(scope);
    }
}

