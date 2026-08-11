package com.atlaspay.shared.money;

import com.atlaspay.shared.exception.ValidationException;
import java.math.BigDecimal;

/**
 * Thrown when a Money amount is negative.
 * Maps to HTTP 400 Bad Request via ValidationException.
 */
public class NegativeMoneyException extends ValidationException {

    public NegativeMoneyException(BigDecimal amount) {
        super("MONEY_NEGATIVE_AMOUNT", "Money amount cannot be negative: " + amount);
    }
}
