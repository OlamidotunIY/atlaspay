# Module Design Document — `atlaspay-app`

> **Status:** `APPROVED`
> **Author:** Antigravity & User
> **Created:** 2026-08-12
> **Last Updated:** 2026-08-12

---

## 1. Overview

### 1.1 Purpose
This is the **Composition Root** of the AtlasPay system. It acts as the central orchestrator that boots the Spring Application Context, wires all independent modules together, and enforces global, cross-cutting concerns (security, API documentation, and standardized error handling).

### 1.2 Scope — What This Module Does
*   **Application Bootstrapping:** Houses the `@SpringBootApplication` main class.
*   **Global Security:** Manages JWT validation, API Key validation, and Spring Security Filter Chains.
*   **Global Exception Handling:** Catches domain exceptions (`NotFoundException`, `BusinessRuleException`) thrown by any module and maps them to a standardized API Error JSON response.
*   **Global Configuration:** Configures Jackson (JSON), Swagger/OpenAPI, CORS, and Async thread pools.

### 1.3 Out of Scope — What This Module Does NOT Do
*   **Zero Business Logic:** No domain models, no use cases, and no repositories.
*   **Zero Domain Controllers:** All REST API controllers (e.g., `AuthController`, `MerchantController`) must live in their respective modules (e.g., `atlaspay-identity`). This module only scans them.

### 1.4 Dependencies
*   Depends on **ALL** modules in the system (to assemble the monolith).

---

## 2. Architecture & Folder Structure

Because this module contains no domain logic, it eschews the Hexagonal Architecture in favor of a specialized, infrastructure-centric layout:

```text
atlaspay-app/src/main/java/com/atlaspay/app/
├── AtlasPayApplication.java 
├── config/                  # OpenApiConfig, WebMvcConfig (CORS), AsyncConfig
├── security/                # SecurityConfig, JwtAuthenticationFilter, ApiKeyAuthenticationFilter
└── exception/               # GlobalExceptionHandler, ApiErrorResponse
```

---

## 3. Global Exception Handling

The `GlobalExceptionHandler` ensures that no matter which module throws an error, the consumer always receives a predictable JSON format.

**Standardized `ApiErrorResponse`:**
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

## 4. Security Mechanics

The app enforces a stateless (stateless session management) filter chain.
1. `JwtAuthenticationFilter`: Extracts Bearer token, validates signature, and establishes `AtlasPayAuthenticationToken`.
2. `ApiKeyAuthenticationFilter`: Validates custom `X-API-KEY` headers for server-to-server integration.
