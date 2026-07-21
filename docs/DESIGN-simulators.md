# AtlasPay — Simulators Design Reference (atlaspay-simulators)

> Test doubles for external bank/card-network integrations. Complements docs/DESIGN.md (core domain model).

## Result/DTO Types

```java
public record AuthorizationReference(String value) {}
public record CaptureReference(String value) {}
public record InboundTransferResult(boolean success, String referenceId, String errorCode) {}
public record OutboundTransferResult(boolean success, String referenceId, String errorCode) {}
public record AuthorizationResult(boolean approved, String authorizationCode, String declineReason) {}
public record CaptureResult(boolean success, String captureReferenceId, String errorCode) {}
public record RefundResult(boolean success, String refundReferenceId, String errorCode) {}
public record ReversalResult(boolean success, String errorCode) {}
public record CardIssuanceResult(boolean success, String cardToken, String errorCode) {}
public record CardStatusResult(String status, boolean active) {}
```
*These DTOs are infrastructure-side response shapes for the simulator ports already referenced by `docs/DESIGN.md`; they stay out of core because they model external-integration outcomes, not domain state.*

## Simulator Implementations

```java
public final class InMemoryBankSimulator implements BankSimulator {
    private int simulatedLatencyMs;
    private double failureRate;
    private Map<String, CannedResponse> scenarios;

    public InMemoryBankSimulator(int latencyMs, double failureRate);
    public InMemoryBankSimulator(BankSimulatorConfig config);

    @Override
    public InboundTransferResult receiveInboundTransfer(ExternalPartyReference sender, Money amount, AccountNumber destination);

    @Override
    public OutboundTransferResult sendOutboundTransfer(AccountNumber source, ExternalPartyReference beneficiary, Money amount);
}

public final class InMemoryCardNetworkSimulator implements CardNetworkSimulator {
    private int simulatedLatencyMs;
    private Map<String, CannedResponse> scenarios;

    public InMemoryCardNetworkSimulator(int latencyMs, Map<String, CannedResponse> scenarios);

    @Override
    public AuthorizationResult authorize(CardToken token, Money amount);

    @Override
    public CaptureResult capture(AuthorizationReference authRef);

    @Override
    public RefundResult refund(CaptureReference captureRef, Money amount);

    @Override
    public ReversalResult reverse(AuthorizationReference authRef);
}

public final class InMemoryIssuingBankSimulator implements IssuingBankSimulator {
    private Map<String, CannedResponse> scenarios;

    public InMemoryIssuingBankSimulator(Map<String, CannedResponse> scenarios);

    @Override
    public CardIssuanceResult issueCard(AccountId linkedAccountId, YearMonth expiry);

    @Override
    public CardStatusResult reportStatus(CardToken token);
}
```
*These are deterministic test doubles, not fake aggregates. They intentionally mimic real integration verbs (`authorize`, `capture`, `refund`, `reverse`) so application wiring can later swap them for true adapters without changing core ports.*

## Configurability Mechanism

```java
public final class BankSimulatorConfig {
    private int latencyMs;
    private double failureRate;
    private Map<String, CannedResponse> scenarios;

    public static BankSimulatorConfig alwaysSucceed();
    public static BankSimulatorConfig alwaysFail(String errorCode);
    public static BankSimulatorConfig scenario(String name, CannedResponse response);
}

public record CannedResponse(boolean success, String code, String message) {}
```
*Test or demo code configures named scenarios up front, then the simulator selects the matching canned outcome for the next call path. For example, a demo can bind scenario name `authorize.card-expired` to `new CannedResponse(false, "CARD_EXPIRED", "Expiry date in past")`, causing the next `authorize()` invocation to return a declined `AuthorizationResult` without needing a live network.*

## Correlation to Core Call Sites

- `BankSimulator.receiveInboundTransfer()` ← called by `TransferSaga` orchestration when progressing an `InboundExternalTransfer` after validation/posting.
- `BankSimulator.sendOutboundTransfer()` ← called by `TransferSaga` orchestration when progressing an `OutboundExternalTransfer` after validation/posting.
- `CardNetworkSimulator.authorize()` ← called by an infrastructure implementation of core's `CardPaymentAuthorizationService` while evaluating a payment against a `Card` and limits.
- `CardNetworkSimulator.capture()` / `refund()` / `reverse()` ← called by card-payment orchestration around `Card`/`Dispute` workflows once the relevant aggregate or saga reaches the needed state.
- `IssuingBankSimulator.issueCard()` ← called when a new `Card` aggregate is being created and tokenized for an `Account`.
- `IssuingBankSimulator.reportStatus()` ← called by support/demo flows that reconcile simulator status back to the `Card` aggregate's lifecycle.
