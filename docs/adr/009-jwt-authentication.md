# ADR-009: JWT (RS256) for API Authentication, OAuth2 Client-Credentials for Outbound Services

## Status
Accepted

## Date
2026-08-11

## Context
AtlasPay has two authentication concerns:

1. **Inbound**: Merchants and sub-accounts authenticate to the AtlasPay API
2. **Outbound**: AtlasPay calls Anchor and Dojah APIs — those calls must be authenticated

Additionally, webhook callbacks from Anchor arrive and must be verified as genuine.

Options for inbound API authentication:
- Session cookies (server-side sessions)
- Opaque tokens (random strings looked up in DB on every request)
- JWT (self-contained, signed tokens verified without DB lookup)

## Decision

### Inbound: JWT with RS256

AtlasPay issues **JWT access tokens signed with RS256** (asymmetric — private key signs,
public key verifies).

- Algorithm: RS256 (not HS256 — asymmetric means the verification key can be public,
  useful if future microservices need to verify tokens independently)
- Access token TTL: 15 minutes
- Refresh token TTL: 7 days (stored in `refresh_tokens` MySQL table for revocation)
- Payload claims:
  ```json
  {
    "sub": "usr_01HV...",
    "role": "MERCHANT",
    "permissions": ["charges:write", "transfers:write", "ledger:read"],
    "tenant": "mrch_01HV...",
    "iat": 1234567890,
    "exp": 1234568790
  }
  ```
- Token blacklisting: revoked tokens are stored in Redis (`SET blacklist:{jti} 1 EX <remaining_ttl>`)
- Spring Security resource server verifies JWT signature using public key from `application.yml`

### Outbound: OAuth2 Client Credentials

Calls to Anchor and Dojah use **OAuth2 client-credentials flow**:
- Spring Security's `OAuth2AuthorizedClientManager` handles token acquisition and refresh
- Tokens cached in memory; refreshed automatically before expiry
- Client ID + secret injected via environment variables (never in code/yml)

### Webhook verification: HMAC-SHA256

Anchor webhooks carry an `X-Anchor-Signature` header — HMAC-SHA256 of the raw request body
using the webhook secret. AtlasPay verifies this before processing any webhook payload.

```java
@Component
public class AnchorWebhookVerifier {
    public boolean verify(String payload, String signature) {
        var expected = hmacSha256(payload, anchorWebhookSecret);
        return MessageDigest.isEqual(
            expected.getBytes(), signature.getBytes()); // constant-time compare
    }
}
```

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Session cookies | Stateful — requires session store (Redis). Does not work for API clients (mobile SDKs, server-to-server). |
| Opaque tokens (DB lookup per request) | Every API request hits the database for token validation. Unacceptable latency at scale. |
| JWT with HS256 | Symmetric key — any service that can verify can also sign. Riskier if a service is compromised. RS256 allows read-only verification keys to be distributed. |
| Passportjs / Auth0 / Cognito (hosted IdP) | Valid for production at scale. Rejected to demonstrate the implementation from first principles as a portfolio project. Can be swapped in via the `AuthenticationPort` interface without touching domain logic. |

## Consequences

### Positive
- JWT verification is stateless — no DB hit per request; scales horizontally
- RS256 asymmetric signing — public key can be shared with future microservices for independent verification
- Permissions embedded in token — method-level `@PreAuthorize` works without DB lookup
- OAuth2 client-credentials is the standard pattern for M2M authentication

### Negative / Trade-offs
- JWT access tokens cannot be instantly revoked (they are valid until expiry)
  → mitigated by short TTL (15 min) + Redis blacklist for critical revocations (logout, compromised token)
- Private key management: RSA key pair must be rotated periodically; stored in Kubernetes Secret / AWS Secrets Manager
- Refresh token rotation must be implemented carefully to prevent replay attacks

## References
- [RFC 7519 — JSON Web Token](https://datatracker.ietf.org/doc/html/rfc7519)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [OAuth 2.0 Client Credentials — RFC 6749](https://datatracker.ietf.org/doc/html/rfc6749#section-4.4)
