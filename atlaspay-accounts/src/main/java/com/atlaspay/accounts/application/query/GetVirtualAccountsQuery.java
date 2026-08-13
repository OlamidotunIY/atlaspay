package com.atlaspay.accounts.application.query;

import com.atlaspay.shared.usecase.Query;

public record GetVirtualAccountsQuery(String ownerId) implements Query {
}
