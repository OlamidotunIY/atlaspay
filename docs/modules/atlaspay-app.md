# Module Design Document — `atlaspay-app`

> **Status:** `APPROVED`
> **Author:** Antigravity & User
> **Created:** 2026-08-12
> **Last Updated:** 2026-08-12

---

## 1. Overview

### 1.1 Purpose
The `atlaspay-app` module is the **Composition Root** of the entire AtlasPay system. It is strictly an infrastructure module that acts as the entry point and orchestrator. It bootstraps the Spring Application Context, wires all independent bounded contexts (Identity, Ledger, Transactions, EventBus, etc.) together, and enforces global, cross-cutting concerns such as Security, API documentation, and standardized Error Handling.

### 1.2 Strict Scope — What This Module Does
1.  **Application Bootstrapping:** Houses `AtlasPayApplication` containing the `@SpringBootApplication` and global `@ComponentScan`.
2.  **Global Security & Authentication:** Manages HTTP security chains, stateless session management, CORS policies, JWT validation filters, and API Key authentication filters.
3.  **Global Exception Handling:** Provides a centralized `@RestControllerAdvice` to catch unified domain exceptions (e.g., `NotFoundException`, `BusinessRuleException`) thrown by any module and map them to a standardized API Error JSON response.
4.  **Cross-Cutting Configuration:** Configures Jackson (JSON serialization/deserialization), Swagger/OpenAPI documentation generation, and Spring MVC Web configurations.
5.  **Environment & Profile Management:** Houses the core `application.yml`, `application-local.yml`, and `application-prod.yml` configuration files that drive properties for all underlying modules (e.g., database URLs, Kafka brokers).

### 1.3 Strict Out of Scope — What This Module Does NOT Do
*   **Zero Business Logic:** No domain models, no use cases, no ports, and no adapters.
*   **Zero Feature Controllers:** All REST API controllers (e.g., `AuthController`, `MerchantController`) live in their respective modules (e.g., `atlaspay-identity`). This module merely scans and exposes them.
*   **Zero Persistence Entities:** JPA Entities belong in the infrastructure layers of their specific domain modules.

### 1.4 Dependencies
*   Depends on **ALL** modules in the system (`atlaspay-identity`, `atlaspay-eventbus`, `atlaspay-ledger`, etc.) to assemble the monolith at runtime.

---

## 2. Architecture & Folder Structure

Because this module contains no domain logic, it completely eschews the Hexagonal Architecture in favor of a specialized, cross-cutting infrastructure layout.

```text
atlaspay-app/src/main/java/com/atlaspay/app/
├── AtlasPayApplication.java            # Spring Boot Entry Point
├── config/                             # Global Application Configuration
│   ├── OpenApiConfig.java              # Swagger / OpenAPI annotations
│   ├── SecurityConfig.java             # Spring Security HttpSecurity DSL
│   └── WebMvcConfig.java               # CORS and Jackson Configuration
├── security/                           # Authentication Mechanisms
│   ├── authentication/                 
│   │   └── AtlasPayAuthenticationToken.java # Custom Authentication implementation
│   └── filter/                         
│       ├── ApiKeyAuthenticationFilter.java  # Intercepts and validates X-API-KEY headers
│       └── JwtAuthenticationFilter.java     # Intercepts and validates Bearer tokens
└── exception/                          # Global Error Translation
    ├── ApiErrorResponse.java           # Standard DTO for all HTTP errors
    └── GlobalExceptionHandler.java     # @RestControllerAdvice mapping Exceptions to 4xx/5xx
```

---

## 3. Global Security Mechanics

The app enforces a **stateless** authentication architecture using Spring Security.

### 3.1 Security Filter Chain (`SecurityConfig`)
*   **Session Management:** Configured as `SessionCreationPolicy.STATELESS`.
*   **Public Endpoints:** explicit paths like `/api/v1/auth/**`, `/v3/api-docs/**`, and `/actuator/health` are permitted without authentication.
*   **Protected Endpoints:** All other endpoints require authentication.
*   **Filter Order:** `ApiKeyAuthenticationFilter` runs first. If an API key is missing, it falls through to `JwtAuthenticationFilter`. Both filters are inserted before Spring's `UsernamePasswordAuthenticationFilter`.

### 3.2 Token Validation
1.  **`JwtAuthenticationFilter`**: 
    *   Extracts the `Authorization: Bearer <token>` header.
    *   Parses the JWT and verifies the signature using a shared secret key configured in `application.yml`.
    *   Extracts the subject (Merchant ID / User ID) and authorities (roles).
    *   Populates the `SecurityContextHolder`.
2.  **`ApiKeyAuthenticationFilter`**: 
    *   Extracts the `X-API-KEY` header (primarily used for server-to-server or SDK communication).
    *   Queries the `atlaspay-identity` module (via a direct use case or query service invocation, if necessary, though ideally the JWT handles most UI traffic).

---

## 4. Global Exception Handling

The `GlobalExceptionHandler` ensures that no matter which internal module throws an error, the external API consumer always receives a predictable JSON format.

### 4.1 Mapped Exceptions
*   **`BusinessRuleException`** ➔ Maps to `400 Bad Request` or `409 Conflict`.
*   **`NotFoundException`** ➔ Maps to `404 Not Found`.
*   **`ValidationException`** ➔ Maps to `422 Unprocessable Entity`.
*   **`AccessDeniedException`** ➔ Maps to `403 Forbidden`.
*   **`Exception` (Unhandled)** ➔ Maps to `500 Internal Server Error` (stack traces are hidden in production).

### 4.2 Standardized `ApiErrorResponse`
```json
{
  "timestamp": "2026-08-12T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "IDENTITY_VALIDATION_FAILED",
  "message": "Email format is invalid",
  "path": "/api/v1/identity/merchants"
}
```

---

## 5. System Wiring Diagram

```mermaid
graph TD
    App[atlaspay-app (Composition Root)]
    
    App --> ID(atlaspay-identity)
    App --> EB(atlaspay-eventbus)
    App --> LED(atlaspay-ledger)
    App --> CHG(atlaspay-charges)
    App --> TX(atlaspay-transactions)

    subgraph "Application Context"
        SecurityConfig --> JwtFilter
        GlobalExceptionHandler
    end
    
    JwtFilter -.-> ID
    ID -. Domain Events .-> EB
```
