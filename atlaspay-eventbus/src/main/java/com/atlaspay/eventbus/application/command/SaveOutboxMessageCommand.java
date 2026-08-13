package com.atlaspay.eventbus.application.command;

import com.atlaspay.shared.event.EnvelopedDomainEvent;

public record SaveOutboxMessageCommand(String topic, EnvelopedDomainEvent<?> event) {
}
