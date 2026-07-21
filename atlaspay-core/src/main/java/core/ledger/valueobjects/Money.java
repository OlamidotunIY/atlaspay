package core.ledger.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

public class Money implements Comparable<Money> {

    private final BigDecimal amount;   // always rescaled to currency's minor-unit precision
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        this.currency = currency;
        this.amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money ofMinorUnits(long minorUnits, Currency currency) {
        BigDecimal major = BigDecimal.valueOf(minorUnits)
                .movePointLeft(currency.getDefaultFractionDigits());
        return new Money(major, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), currency);
    }

    public Money negate() {
        return new Money(this.amount.negate(), currency);
    }

    public Money multiply(BigDecimal factor, RoundingMode roundingMode) {
        Objects.requireNonNull(factor, "factor must not be null");
        Objects.requireNonNull(roundingMode, "roundingMode must not be null");
        BigDecimal result = this.amount.multiply(factor)
                .setScale(currency.getDefaultFractionDigits(), roundingMode);
        return new Money(result, currency);
    }

    public List<Money> allocate(int parts) {
        if (parts <= 0) {
            throw new IllegalArgumentException("parts must be positive");
        }
        long totalMinorUnits = this.amount
                .movePointRight(currency.getDefaultFractionDigits())
                .longValueExact();

        long base = totalMinorUnits / parts;
        long remainder = totalMinorUnits % parts;

        List<Money> shares = new ArrayList<>(parts);
        for (int i = 0; i < parts; i++) {
            long share = base + (i < remainder ? 1 : 0); // largest-remainder method
            shares.add(Money.ofMinorUnits(share, currency));
        }
        return shares;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException(this.currency, other.currency);
        }
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return amount.equals(other.amount) && currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency.getCurrencyCode();
    }
}
