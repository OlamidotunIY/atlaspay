package com.atlaspay.accounts.application.command;

import com.atlaspay.shared.usecase.Command;

public record RequestClosureCommand(String accountId) implements Command {
}
