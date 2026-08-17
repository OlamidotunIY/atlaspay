package com.atlaspay.ledger.domain.exception;

import com.atlaspay.shared.exception.ErrorCode;

public enum LedgerErrorCode implements ErrorCode {
    INVALID_TRANSACTION_STATE,
    UNBALANCED_TRANSACTION,
    TRANSACTION_ALREADY_POSTED,
    ACCOUNT_NOT_FOUND,
    UNAUTHORIZED_ACCESS,
    CURRENCY_MISMATCH
}
