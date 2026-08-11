package com.atlaspay.shared.money;

import com.atlaspay.shared.exception.SharedErrorCode;
import com.atlaspay.shared.exception.ValidationException;
import java.math.BigDecimal;

public class NegativeMoneyException extends ValidationException {

    public NegativeMoneyException(BigDecimal amount) {
        super(SharedErrorCode.MONEY_NEGATIVE_AMOUNT, "Money amount cannot be negative: " + amount);
    }
}
