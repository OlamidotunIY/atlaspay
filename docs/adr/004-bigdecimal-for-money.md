# ADR-004: Use BigDecimal (Never double/float) for All Monetary Values

## Status
Accepted

## Date
2026-08-11

## Context
Floating-point types (`double`, `float`) use IEEE 754 binary representation.
Most decimal fractions cannot be represented exactly in binary:

```java
System.out.println(0.1 + 0.2);  // prints 0.30000000000000004
System.out.println(1000.00 * 0.15);  // prints 149.99999999999997
```

In a general-purpose application this is an acceptable rounding artefact.
In a payment system it is a critical bug: fee calculations, split allocations,
and settlement amounts must be exact to the last kobo.

Real-world payment bugs caused by floating-point:
- Fee splits that don't add up to the charged amount → unexplained balance discrepancies
- Rounding errors accumulate over thousands of transactions → ledger out of balance
- `0.1 NGN` represented as `0.09999999999...` → stored in DB as a different value than computed

## Decision
**All monetary values in AtlasPay use `BigDecimal` exclusively.** This is enforced at
every layer of the stack:

### Application layer
- `Money` value object wraps `BigDecimal` + `CurrencyCode`:
  ```java
  public record Money(BigDecimal amount, CurrencyCode currency) {
      public Money {
          Objects.requireNonNull(amount);
          Objects.requireNonNull(currency);
          if (amount.compareTo(BigDecimal.ZERO) < 0)
              throw new NegativeMoneyException(amount);
      }
      public Money add(Money other)      { /* currency-safe addition */ }
      public Money subtract(Money other) { /* currency-safe subtraction */ }
      public Money multiply(BigDecimal factor, RoundingMode mode) { ... }
  }
  ```
- Rounding mode: `RoundingMode.HALF_EVEN` (banker's rounding) at every arithmetic boundary
- Scale: always 4 decimal places (`setScale(4, HALF_EVEN)`) before persistence

### Persistence layer
- MySQL column type: `DECIMAL(19, 4)` — never `DOUBLE`, `FLOAT`, or `NUMERIC` without precision
- Hibernate mapping: `@Column(columnDefinition = "DECIMAL(19,4)")` on all amount fields

### API layer
- JSON serialisation: `BigDecimal` serialised as a JSON number string `"1234.5000"` (not float)
  via custom `MoneySerializer`/`MoneyDeserializer` — prevents JavaScript client precision loss
- OpenAPI spec: monetary fields typed as `string` format `decimal` with example `"1500.0000"`

### Enforcement
- ArchUnit test rule: fails build if any field annotated `@Monetary` uses `double`/`float`/`Double`/`Float`
- Code review checklist: "No floating-point for money" is the first item

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| `double` / `Double` | Binary floating-point — inherently imprecise for decimal fractions. Categorically rejected. |
| `long` (store pence/kobo as integer) | Avoids floating-point but requires application-level scale tracking; error-prone when mixing currencies with different minor-unit counts. BigDecimal is safer and self-documenting. |
| `javax.money.MonetaryAmount` (JSR 354) | Valid; provides currency-aware arithmetic. Rejected for now — adds a dependency, less familiar to most Java developers, and `BigDecimal`-based `Money` value object achieves the same guarantees with simpler code. |

## Consequences

### Positive
- Fee calculations are exact: `1500.00 * 0.015 = 22.5000` always
- Ledger double-entry always balances: `SUM(debits) == SUM(credits)` provable
- `DECIMAL(19,4)` in MySQL means DB storage is also exact — no precision loss at rest
- JSON string format prevents JavaScript `Number` precision loss on large amounts

### Negative / Trade-offs
- `BigDecimal` arithmetic is more verbose than `double` arithmetic
  → mitigated by the `Money` value object which encapsulates all operations
- `BigDecimal` is slower than `double` for raw arithmetic
  → irrelevant at payment transaction volumes; correctness > micro-optimisation

## References
- [What Every Programmer Should Know About Floating-Point](https://floating-point-gui.de/)
- [JSR-354: Money and Currency API](https://jcp.org/en/jsr/detail?id=354)
- [Effective Java 3rd Ed., Item 60: Avoid float and double if exact answers are required](https://www.oreilly.com/library/view/effective-java-3rd/9780134686097/)
