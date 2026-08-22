package com.atlaspay.admin.domain.event;

import com.atlaspay.shared.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record AdminCompanyEmailGenerated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<AdminCompanyEmailGenerated.Payload> {

    public record Payload(
        Long adminId,
        String employeeCode,
        String companyEmail
    ) {}

    public AdminCompanyEmailGenerated(Long adminId, String employeeCode, String companyEmail) {
        this(
            UUID.randomUUID().toString(),
            String.valueOf(adminId),
            ZonedDateTime.now(),
            new Payload(adminId, employeeCode, companyEmail)
        );
    }
}
