# Module Design Document — `atlaspay-rate-limiter`

> **Status:** `IN REVIEW`
> **Author:** Antigravity (AI Assistant)
> **Created:** 2026-08-12
> **Last Updated:** 2026-08-12

---

## 1. Overview

### 1.1 Purpose
The `atlaspay-rate-limiter` module provides a distributed rate-limiting infrastructure across the AtlasPay ecosystem. It is designed to protect the system from abuse, brute-force attacks, and distributed denial-of-service (DDoS) attacks, while also enforcing business-level API quotas per merchant or API key.

### 1.2 Scope — What This Module Does
- Provides the core Rate Limiter abstraction interfaces.
- Implements the **Token Bucket** algorithm (for general API traffic) and **Sliding Window Counter** algorithm (for strict protection on sensitive endpoints).
- Manages distributed state using **Redis** to ensure limits are enforced consistently across multiple application instances.
- Exposes a custom `@RateLimit` annotation and Spring AOP Aspect for declarative, business-specific rate limiting on controllers or use cases.
- Provides a global HandlerInterceptor/Filter for IP-based DDOS protection on the API Gateway layer (`atlaspay-app`).

### 1.3 Out of Scope — What This Module Does NOT Do
- It does not implement business logic for billing merchants for API overages.
- It does not store API Keys or Merchant profiles (that belongs to the `atlaspay-identity` module).
- It does not act as a reverse proxy; it is a library/module included within the Spring Boot application context.

### 1.4 Dependencies

| Dependency | Type | Reason |
|---|---|---|
| `atlaspay-shared-kernel` | Internal module | Exception handling (`RateLimitExceededException`) |
| `spring-boot-starter-data-redis` | External | Distributed state storage for buckets and windows |

---

## 2. Domain Model

Since this is an infrastructural module, it doesn't have traditional Aggregate Roots or Entities that map to a relational database. Its domain consists of the policies and algorithms.

### 2.3 Enums

| Enum | Values | Notes |
|---|---|---|
| `RateLimitAlgorithm` | `TOKEN_BUCKET`, `SLIDING_WINDOW` | Defines which algorithm to use. |
| `RateLimitKeyType` | `IP`, `MERCHANT_ID`, `API_KEY` | Defines how the unique key for the limit is extracted from the request. |

---

## 3. Domain Events
(None - This module does not publish domain events as it operates strictly on synchronous HTTP request interceptors.)

---

## 4. Repository Ports
(None - This module uses Redis directly via `StringRedisTemplate` instead of JPA Repositories.)

---

## 5. Application Layer (CQRS)

This module provides infrastructural services and AOP aspects rather than traditional CQRS commands/queries.

### Abstraction Ports

```java
public interface RateLimiter {
    boolean isAllowed(String key, RateLimitPolicy policy);
}
```

**Policy Definition:**
```java
public record RateLimitPolicy(
    RateLimitAlgorithm algorithm,
    int capacity,             // Max tokens for Token Bucket
    int refillRatePerSecond,  // Refill rate for Token Bucket
    int windowSizeSeconds,    // Window size for Sliding Window
    int maxRequests           // Threshold for Sliding Window
) {}
```

---

## 6. Outbound Ports (External Dependencies)
(None beyond Redis itself, which is handled via Spring Data Redis adapters internally.)

---

## 7. REST API Surface

This module does **not** expose its own REST API endpoints. Instead, it intercepts requests to other modules' endpoints.

### Global Exception Handling

When a limit is breached, the module throws a `RateLimitExceededException`. 
The global exception handler in the composition root (`atlaspay-app`) will intercept this and return:

**Error Response:**
```json
{
  "errorCode": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests. Please try again later.",
  "status": 429
}
```

**Headers:**
- `X-RateLimit-Remaining: 0`
- `X-RateLimit-Reset: [timestamp]`

---

## 8. Database

(No Relational Database tables. Uses Redis.)

### Redis Key Structures

- **Token Bucket Key:** `rate_limit:tb:{key}` -> hash containing `tokens` and `lastRefill`
- **Sliding Window Key:** `rate_limit:sw:{key}` -> Sorted Set (ZSET) storing timestamps as scores.

---

## 9. Error Codes

All error codes are defined in `RateLimiterErrorCode` enum.

| Error Code | Exception Type | HTTP Status | When Thrown |
|---|---|---|---|
| `RATE_LIMIT_EXCEEDED` | `RateLimitExceededException` | 429 | When a client exceeds their allocated quota (Token Bucket or Sliding Window). |

---

## 10. Dynamic Rules & Worker Sync

Instead of relying solely on hardcoded values in annotations, the rate limiter supports **Dynamic Rules**.
- Rules define the limits (capacity, refill rate, window size) per endpoint, per IP, or per Merchant ID.
- Rules are persisted in the system (e.g., in a database or configuration file) and a background **Worker** (e.g., a `@Scheduled` job or an event listener) periodically syncs these rules into Redis.
- The Redis Lua scripts read these rules directly from Redis (e.g., from a Redis Hash `rate_limit:rules:{ruleId}`) to determine the limits dynamically at runtime, ensuring configuration changes apply globally without restarting instances.

## 11. Implementation Decisions
- **No external libraries**: We will not use Bucket4j or similar libraries. The Token Bucket and Sliding Window algorithms will be implemented using highly optimized, custom **Redis Lua scripts** to guarantee atomicity and performance.
