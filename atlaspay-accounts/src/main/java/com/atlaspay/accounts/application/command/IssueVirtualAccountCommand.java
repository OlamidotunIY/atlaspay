package com.atlaspay.accounts.application.command;

import com.atlaspay.accounts.domain.model.OwnerType;
import com.atlaspay.shared.annotation.IdempotencyKey;
import com.atlaspay.shared.usecase.Command;

public record IssueVirtualAccountCommand(
        @IdempotencyKey String idempotencyKey, 
        String ownerId, 
        OwnerType ownerType, 
        String bankName,
        String country
) implements Command {
}
