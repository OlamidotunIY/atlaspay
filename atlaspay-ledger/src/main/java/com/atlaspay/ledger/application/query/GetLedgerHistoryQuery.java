package com.atlaspay.ledger.application.query;

public record GetLedgerHistoryQuery(Long integration, int page, int perPage) {}
