package com.atlaspay.eventbus.application.command;

public record ProcessOutboxMessagesCommand(int batchSize) {
}
