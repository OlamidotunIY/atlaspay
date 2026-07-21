package core.access;

import core.access.valueobjects.ApiKeyId;
import core.access.valueobjects.ApiKeyStatus;
import core.access.valueobjects.HashedSecret;
import core.access.valueobjects.Scope;
import core.identity.valueobject.CompanyId;
import core.shared.AggregateRoot;

import java.util.Set;

public final class ApiKey extends AggregateRoot<ApiKeyId> {
    private final CompanyId ownerCompanyId;
    private final HashedSecret hashedSecret;          // never stores plaintext secret
    private final Set<Scope> scopes;                    // unmodifiable
    private ApiKeyStatus status;                          // ACTIVE, REVOKED

    public ApiKey(ApiKeyId apiKeyId, CompanyId ownerCompanyId, HashedSecret hashedSecret, Set<Scope> scopes) {
        super(apiKeyId);
        this.ownerCompanyId = ownerCompanyId;
        this.hashedSecret = hashedSecret;
        this.scopes = scopes;
    }

    public void revoke() {}
    public boolean hasScope(Scope scope) {
        return true;
    }
}

