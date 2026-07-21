package core.access;

import core.access.valueobjects.ApiKeyId;
import core.access.valueobjects.HashedSecret;
import core.shared.Repository;

import java.util.Optional;

public interface ApiKeyRepository extends Repository<ApiKey, ApiKeyId> {
    Optional<ApiKey> findByHashedSecret(HashedSecret hashedSecret); // authentication lookup
}

