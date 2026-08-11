package com.atlaspay.shared.money;

import com.atlaspay.shared.exception.AtlasPayException;
import java.math.BigDecimal;

public class NegativeMoneyException extends AtlasPayException {
    public NegativeMoneyException(BigDecimal amount) {
        super("Money amount cannot be negative: " + amount);
    }
}
