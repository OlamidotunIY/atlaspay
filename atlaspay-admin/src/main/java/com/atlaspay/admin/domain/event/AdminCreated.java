package com.atlaspay.admin.domain.event;

import com.atlaspay.shared.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record AdminCreated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<AdminCreated.Payload> {
    
    public record Payload(
        Long adminId,
        String employeeCode,
        String fullName,
        String personalEmail,
        String role
    ) {}

    public AdminCreated(Long adminId, String employeeCode, String fullName, String personalEmail, String role) {
        this(
            UUID.randomUUID().toString(),
            String.valueOf(adminId),
            ZonedDateTime.now(),
            new Payload(adminId, employeeCode, fullName, personalEmail, role)
        );
    }
}
