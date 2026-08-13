package com.atlaspay.accounts.application.command;

import com.atlaspay.shared.usecase.Command;

public record ForceCloseAccountsCommand(String ownerId) implements Command {
}
