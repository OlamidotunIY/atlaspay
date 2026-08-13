package com.atlaspay.accounts.application.command;

import com.atlaspay.shared.annotation.IdempotencyKey;
import com.atlaspay.shared.usecase.Command;

public record IssueVirtualAccountCommand(
        Long integration,
        String customerCode,
        String accountName,
        String bankName,
        @IdempotencyKey String idempotencyKey
) implements Command {
}
