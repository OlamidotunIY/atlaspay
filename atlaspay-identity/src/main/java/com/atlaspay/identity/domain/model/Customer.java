package com.atlaspay.identity.domain.model;

import com.atlaspay.identity.domain.event.CustomerCreated;
import com.atlaspay.identity.domain.event.CustomerProfileUpdated;
import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.domain.id.CustomerId;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.domain.valueobject.PhoneNumber;
import com.atlaspay.shared.exception.SharedErrorCode;
import com.atlaspay.shared.exception.ValidationException;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class Customer extends AggregateRoot<CustomerId> {

    private final CustomerId id;
    private final MerchantId merchantId;
    private String firstName;
    private String lastName;
    private final EmailAddress email;
    private PhoneNumber phone;
    private Map<String, String> metadata;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Customer(CustomerId id, MerchantId merchantId, String firstName, String lastName, EmailAddress email, PhoneNumber phone, Map<String, String> metadata) {
        if (id == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Customer ID is required");
        if (merchantId == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Merchant ID is required");
        if (firstName == null || firstName.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "First name is required");
        if (lastName == null || lastName.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Last name is required");
        if (email == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Email is required");

        this.id = id;
        this.merchantId = merchantId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = this.createdAt;

        registerEvent(new CustomerCreated(
            UUID.randomUUID().toString(),
            this.id.value(),
            this.createdAt,
            new CustomerCreated.Payload(
                this.merchantId.value(),
                this.email.value(),
                this.firstName,
                this.lastName
            )
        ));
    }

    public void updateProfile(String firstName, String lastName, PhoneNumber phone) {
        if (firstName == null || firstName.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "First name is required");
        if (lastName == null || lastName.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Last name is required");

        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(new CustomerProfileUpdated(
            UUID.randomUUID().toString(),
            this.id.value(),
            this.updatedAt,
            new CustomerProfileUpdated.Payload(
                this.merchantId.value(),
                this.firstName,
                this.lastName,
                this.phone != null ? this.phone.value() : null
            )
        ));
    }

    public void updateMetadata(Map<String, String> metadata) {
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public CustomerId getId() {
        return id;
    }
}
