package com.atlaspay.simulator.application.command;

import com.atlaspay.shared.usecase.Command;

public record GenerateSimulatorAccountCommand(
    String referenceId,
    String accountName,
    String callbackUrl,
    String bankName
) implements Command {}
