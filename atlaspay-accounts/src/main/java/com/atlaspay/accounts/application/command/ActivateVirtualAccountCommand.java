package com.atlaspay.accounts.application.command;

import com.atlaspay.shared.usecase.Command;

public record ActivateVirtualAccountCommand(String referenceId, String nuban) implements Command {
}
