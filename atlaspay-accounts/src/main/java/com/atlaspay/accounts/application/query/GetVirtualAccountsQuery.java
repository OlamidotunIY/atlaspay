package com.atlaspay.accounts.application.query;

import com.atlaspay.shared.usecase.Query;

public record GetVirtualAccountsQuery(Long integration) implements Query {
}
