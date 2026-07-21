# AtlasPay — API Design Reference (atlaspay-api)

> REST controllers and DTOs for external/admin HTTP access. Extracted and extended from docs/DESIGN.md § API Layer.

## Module Structure

Package-by-feature plus cross-cutting infrastructure:

```
api/
  identity/                    # Identity & Onboarding API
    CompanyController
    CustomerController
    KycController
    [DTOs: RegisterCompanyRequest, OnboardCustomerRequest, etc.]
  accounts/                    # Accounts API
    AccountController
    [DTOs: OpenAccountRequest, AccountResponse, etc.]
  ledger/                      # Ledger API (internal-admin only)
    LedgerController
    [DTOs: LedgerEntryResponse, etc.]
  transfers/                   # Transfers API
    TransferController
    [DTOs: InitiateTransferRequest, TransferResponse, etc.]
  cards/                       # Cards API
    CardController
    DisputeController
    [DTOs: IssueCardRequest, FileDisputeRequest, etc.]
  limits/                      # Limits API
    LimitController
    [DTOs: CreateLimitPolicyRequest, LimitPolicyResponse, etc.]
  access/                      # Platform Access API
    ApiKeyController
    WebhookController
    [DTOs: IssueApiKeyRequest, CreateWebhookSubscriptionRequest, etc.]
  security/                    # Security infrastructure
    ExternalApiSecurityConfig
    InternalAdminSecurityConfig
    ApiKeyAuthenticationFilter
    CompanyIdScopingFilter
  idempotency/                 # Idempotency-Key enforcement
    IdempotencyKeyFilter
    IdempotencyCache
    CachedResponse
  errors/                      # Error handling
    ErrorResponse
    GlobalExceptionHandler
```

*Each feature package mirrors a core domain. The `security/`, `idempotency/`, and `errors/` packages are cross-cutting API infrastructure.*

## Access Boundaries

Every controller below is tagged **`[External]`** or **`[Internal-Admin]`**:

- **`[External]`** — reachable by a company integrator, authenticated via `ApiKey` (`Authorization: Bearer <key>`), authorized per-request against the key's `Scope`s, and always implicitly scoped to that key's own `CompanyId` — an external caller can never address another company's data regardless of what path/id it supplies.
- **`[Internal-Admin]`** — reachable only by AtlasPay's own operations/compliance staff, on a separate, non-public route and a different Spring Security filter chain (e.g. staff SSO/OIDC session auth, never `ApiKey` auth). These exist because some decisions (compliance verification, platform-wide limit policy, bootstrapping a company's very first credential) are the platform's to make, not the integrator's — mixing that authority into the `ApiKey`-authenticated surface would let a compromised key escalate beyond its own company's data.

This distinction was previously implicit; it is now called out explicitly because several operations documented in earlier sections are **intentionally not** externally reachable at all — by design, not by omission. Each such case is noted where it comes up below.

## Identity & Onboarding

```java
public record RegisterCompanyRequest(String name, String registrationNumber) {}
public record CompanyResponse(UUID companyId, String name, String status) {}
public record OnboardCustomerRequest(String fullName, LocalDate dateOfBirth, AddressDto address) {}
public record AddressDto(String line1, String line2, String city, String postalCode, String countryCode) {}
public record CustomerResponse(UUID customerId, String kycStatus, String kycTier) {}
public record VerifyCompanyRequest(boolean approved, String notes) {}
```

```java
@RestController
@RequestMapping("/v1/admin/companies")
public class CompanyAdminController {
    public ResponseEntity<CompanyResponse> registerCompany(@RequestBody RegisterCompanyRequest request);
    public ResponseEntity<CompanyResponse> verifyCompany(@PathVariable UUID companyId, @RequestBody VerifyCompanyRequest request);
}

@RestController
@RequestMapping("/v1/companies")
public class CompanyController {
    public ResponseEntity<CompanyResponse> getOwnCompany();
}

@RestController
@RequestMapping("/v1/customers")
public class CustomerController {
    public ResponseEntity<CustomerResponse> onboardCustomer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody OnboardCustomerRequest request);

    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID customerId);
    public ResponseEntity<List<CustomerResponse>> listCustomers();
}
```
*`Customer.applyKycTier` and `Customer.recordKycDecision` have deliberately **no** corresponding endpoint anywhere because tier/status changes are always the output of `KycEvaluationSaga` driving `KycRuleEngine` evaluation, never a caller-supplied mutation. `CompanyAdminController.registerCompany` and `verifyCompany` are `[Internal-Admin]` for the same reason: platform onboarding/compliance approval is AtlasPay authority, not integrator authority.*

## Limits

```java
public record CreateLimitPolicyRequest(String kycTier, BigDecimal maxSingleTransaction, BigDecimal maxRollingWindow, String currency, Duration rollingWindowDuration) {}
public record LimitPolicyResponse(UUID limitPolicyId, String kycTier, BigDecimal maxSingleTransaction, BigDecimal maxRollingWindow) {}
public record LimitStatusResponse(BigDecimal maxSingleTransaction, BigDecimal remainingInWindow, Instant windowResetsAt) {}
```

```java
@RestController
@RequestMapping("/v1/admin/limit-policies")
public class LimitPolicyAdminController {
    public ResponseEntity<LimitPolicyResponse> createLimitPolicy(@RequestBody CreateLimitPolicyRequest request);
    public ResponseEntity<List<LimitPolicyResponse>> listLimitPolicies();
}

@RestController
@RequestMapping("/v1/accounts")
public class LimitStatusController {
    public ResponseEntity<LimitStatusResponse> getLimitStatus(@PathVariable UUID accountId);
}
```
*Limit policy creation remains `[Internal-Admin]` because tier-based thresholds are a platform policy artifact. Limit status is `[External]` read-only visibility over policy/application results, so integrators can preflight transfers and card actions without owning the policy itself.*

## Platform Access

```java
public record IssueApiKeyRequest(UUID companyId, Set<String> scopes) {}
public record ApiKeyResponse(UUID apiKeyId, String secret, Set<String> scopes) {}
public record CreateWebhookSubscriptionRequest(URI callbackUrl, Set<String> eventTypes) {}
public record WebhookSubscriptionResponse(UUID subscriptionId, URI callbackUrl, String status) {}
public record WebhookDeliveryResponse(UUID deliveryId, String status, int attemptCount) {}
```

```java
@RestController
@RequestMapping("/v1/admin/api-keys")
public class ApiKeyAdminController {
    public ResponseEntity<ApiKeyResponse> issueFirstApiKey(@RequestBody IssueApiKeyRequest request);
}

@RestController
@RequestMapping("/v1/api-keys")
public class ApiKeyController {
    public ResponseEntity<ApiKeyResponse> rotateApiKey();
    public ResponseEntity<Void> revokeApiKey(@PathVariable UUID apiKeyId);
}

@RestController
@RequestMapping("/v1/webhook-subscriptions")
public class WebhookSubscriptionController {
    public ResponseEntity<WebhookSubscriptionResponse> createSubscription(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody CreateWebhookSubscriptionRequest request);

    public ResponseEntity<Void> pauseSubscription(@PathVariable UUID subscriptionId);
    public ResponseEntity<Void> resumeSubscription(@PathVariable UUID subscriptionId);
    public ResponseEntity<List<WebhookDeliveryResponse>> listDeliveries(@PathVariable UUID subscriptionId);
}
```
*The first `ApiKey` must be issued by an internal onboarding flow because a brand-new company has nothing to authenticate with yet. `WebhookSubscriptionController` exposes `pause`/`resume`, but not `disable`, matching the domain split where only platform retry policy may permanently disable delivery.*

## Transfers, Cards, Disputes, Accounts

```java
public record CreateTransferRequest(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount, String currency) {}
public record TransferResponse(UUID transferId, String status, BigDecimal amount, String currency) {}
public record IssueCardRequest(UUID accountId, YearMonth expiry) {}
public record CardResponse(UUID cardId, String status, YearMonth expiry) {}
public record FileDisputeRequest(UUID cardId, UUID transferId, String reasonCode) {}
public record AccountResponse(UUID accountId, String accountNumber, String status, String type) {}
public record BalanceResponse(UUID accountId, BigDecimal amount, String currency, Instant asOf) {}
```

```java
@RestController
@RequestMapping("/v1/transfers")
public class TransferController {
    public ResponseEntity<TransferResponse> createTransfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody CreateTransferRequest request);

    public ResponseEntity<TransferResponse> getTransfer(@PathVariable UUID transferId);
}

@RestController
@RequestMapping("/v1/cards")
public class CardController {
    public ResponseEntity<CardResponse> issueCard(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody IssueCardRequest request);

    public ResponseEntity<CardResponse> getCard(@PathVariable UUID cardId);
    public ResponseEntity<Void> blockCard(@PathVariable UUID cardId);
}

@RestController
@RequestMapping("/v1/disputes")
public class DisputeController {
    public ResponseEntity<Void> fileDispute(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody FileDisputeRequest request);
}

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {
    public ResponseEntity<List<AccountResponse>> listAccountsForCompany(@RequestParam UUID companyId);
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID accountId);
}
```
*Every state-changing endpoint requires `Idempotency-Key` — EIP **Idempotent Receiver** at the HTTP boundary. DTOs remain decoupled from aggregates so HTTP contracts can evolve independently. `Dispute.resolve` and the saga transition methods stay off the public surface because process managers own those flows.*

## Idempotency-Key Enforcement

```java
public record CachedResponse(int httpStatus, Map<String, String> headers, String body) {}

@Component
public class IdempotencyKeyFilter implements Filter {
    private final IdempotencyCache cache;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain);
}

public interface IdempotencyCache {
    Optional<CachedResponse> get(CompanyId companyId, String key);
    void put(CompanyId companyId, String key, CachedResponse response, Duration ttl);
}
```
*Idempotency is scoped by `(CompanyId, Idempotency-Key)` rather than key alone, so two tenants can safely reuse the same client-generated token. The filter short-circuits duplicate POSTs with the original serialized response, keeping controller code thin.*

## Authentication/Authorization Filters

```java
@Configuration
public class ExternalApiSecurityConfig {
    @Bean
    public SecurityFilterChain externalFilterChain(HttpSecurity http);
}

@Configuration
public class InternalAdminSecurityConfig {
    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http);
}

public final class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain);
}

public final class ScopeAuthorizationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain);
}

public final class CompanyIdScopingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain);
}
```
*Two filter chains enforce the access-boundary split. External traffic uses bearer `ApiKey` authentication plus scope and tenant scoping filters; internal admin routes live under `/v1/admin/*` and use staff SSO/OIDC only. `ApiKeyAuthenticationFilter` hashes the presented secret, calls `ApiKeyRepository.findByHashedSecret(...)`, verifies status, and populates the Spring `SecurityContext`.*

## Error Response Shape

```java
public record ErrorResponse(String code, String message, List<String> details, Instant timestamp) {}
```

| Source | HTTP mapping |
|---|---|
| `Result.Err` | `400 Bad Request` |
| `OptimisticLockingFailureException` | `409 Conflict` |
| Aggregate invariant violation (`IllegalStateException`) | `422 Unprocessable Entity` |
| Missing resource | `404 Not Found` |
| Unauthorized | `401 Unauthorized` |
| Forbidden (scope violation) | `403 Forbidden` |

*The API adapter translates domain/application outcomes into transport semantics; it does not leak Java exceptions or aggregate internals directly over HTTP.*

## Request Validation

- Bean Validation (`@NotNull`, `@Valid`, `@Size`, `@Pattern`) applies to request DTOs for **shape checks** only.
- Domain rules (KYC tier requirements, limit policies, account status, dispute eligibility) are **not** re-implemented at the API layer.
- API validation covers only required presence, format, length, and syntactic correctness (UUIDs, dates, currency codes, URI shape).
- Business validation remains in core so the domain model is the single source of truth.

*This is DDD adapter discipline: controllers reject malformed input, then delegate all real business decisions to core services/aggregates.*

## Controller-to-Core Use-Case Mapping

| Controller method | Core use case(s) it drives |
|---|---|
| `CompanyAdminController.registerCompany` | `new Company(...)` + `CompanyRepository.save(...)` |
| `CompanyAdminController.verifyCompany` | `CompanyRepository.findById(...)` + `Company.verify(...)` + save |
| `CompanyController.getOwnCompany` | `CompanyRepository.findById(...)` |
| `CustomerController.onboardCustomer` | `new Customer(...)`, `new KycCase(...)`, `new KycEvaluationSaga(...)` + repository saves |
| `CustomerController.getCustomer/listCustomers` | `CustomerRepository.findById(...)` / `findAll(...)` |
| `LimitPolicyAdminController.createLimitPolicy/listLimitPolicies` | `new LimitPolicy(...)` + `LimitPolicyRepository` |
| `LimitStatusController.getLimitStatus` | `LimitEvaluationService` + read-only repository lookups |
| `ApiKeyAdminController.issueFirstApiKey` | `new ApiKey(...)` + `ApiKeyRepository.save(...)` |
| `ApiKeyController.rotateApiKey/revokeApiKey` | `ApiKeyRepository.findById(...)`, `new ApiKey(...)` or `ApiKey.revoke()` + save |
| `WebhookSubscriptionController.createSubscription/pauseSubscription/resumeSubscription/listDeliveries` | `new WebhookSubscription(...)`, `WebhookSubscription.pause()`, `WebhookSubscription.resume()`, `WebhookDeliveryRepository.findBySubscriptionId(...)` |
| `TransferController.createTransfer/getTransfer` | `Transfer` subtype construction + `TransferRepository`, `TransferSagaRepository` |
| `CardController.issueCard/getCard/blockCard` | `IssuingBankSimulator.issueCard(...)`, `new Card(...)`, `CardRepository.findById(...)`, `Card.block(...)` |
| `DisputeController.fileDispute` | `new Dispute(...)` + `new ChargebackSaga(...)` + repository saves |
| `AccountController.listAccountsForCompany/getBalance` | `AccountRepository.findAll(...)` + tenant scoping, `BalanceCalculator.currentBalance(...)` |
*Every controller above maps to an existing aggregate, repository port, saga, or domain service already described in `docs/DESIGN.md`; none invents a business capability absent from core.*
