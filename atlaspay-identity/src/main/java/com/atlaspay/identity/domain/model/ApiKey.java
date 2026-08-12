package com.atlaspay.identity.domain.model;

import com.atlaspay.identity.domain.event.ApiKeyGenerated;
import com.atlaspay.identity.domain.event.ApiKeyRevoked;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.domain.id.ApiKeyId;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.shared.exception.SharedErrorCode;
import com.atlaspay.shared.exception.ValidationException;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class ApiKey extends AggregateRoot<ApiKeyId> {

    private final ApiKeyId id;
    private final MerchantId merchantId;
    private final KeyType keyType;
    private final ApiEnvironment environment;
    private final String keyHash;
    private final String displayValue;
    private final String prefix;
    private boolean active;
    private final ZonedDateTime createdAt;
    private ZonedDateTime revokedAt;

    public ApiKey(ApiKeyId id, MerchantId merchantId, KeyType keyType, ApiEnvironment environment, 
                  String keyHash, String displayValue, String prefix) {
        if (id == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "API Key ID is required");
        if (merchantId == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Merchant ID is required");
        if (keyType == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "KeyType is required");
        if (environment == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Environment is required");
        if (keyHash == null || keyHash.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Key Hash is required");
        if (displayValue == null || displayValue.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Display Value is required");
        if (prefix == null || prefix.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Prefix is required");

        this.id = id;
        this.merchantId = merchantId;
        this.keyType = keyType;
        this.environment = environment;
        this.keyHash = keyHash;
        this.displayValue = displayValue;
        this.prefix = prefix;
        this.active = true;
        this.createdAt = ZonedDateTime.now();

        registerEvent(new ApiKeyGenerated(
            UUID.randomUUID().toString(),
            this.id.value(),
            this.createdAt,
            new ApiKeyGenerated.Payload(
                this.merchantId.value(),
                this.keyType,
                this.environment,
                this.prefix
            )
        ));
    }

    public void revoke() {
        if (!this.active) {
            throw new BusinessRuleException(IdentityErrorCode.API_KEY_ALREADY_REVOKED, "API Key is already revoked");
        }
        
        this.active = false;
        this.revokedAt = ZonedDateTime.now();

        registerEvent(new ApiKeyRevoked(
            UUID.randomUUID().toString(),
            this.id.value(),
            this.revokedAt,
            new ApiKeyRevoked.Payload(
                this.merchantId.value(),
                this.keyType,
                this.environment
            )
        ));
    }

    public MerchantId getMerchantId() {
        return merchantId;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public ApiKeyId getId() {
        return id;
    }
}
