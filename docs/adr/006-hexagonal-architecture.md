# ADR-006: Hexagonal Architecture (Ports and Adapters) Per Module

## Status
Accepted

## Date
2026-08-11

## Context
AtlasPay integrates with external providers (Anchor, Dojah, Kafka, MySQL, Redis) whose
APIs, schemas, and availability are outside our control. If domain logic is entangled
with infrastructure concerns:

- Swapping a provider (e.g., replacing Anchor with a different BaaS) requires rewriting business logic
- Unit-testing domain logic requires real databases or HTTP servers
- Kafka and MySQL details leak into aggregate roots and use cases
- Changes to the Anchor API break unrelated tests

## Decision
Every module in AtlasPay follows the **Hexagonal Architecture** (Ports and Adapters)
four-layer structure. Dependencies flow strictly **inward**:

```
presentation  →  application  →  domain
infrastructure →  application  →  domain
         (infrastructure never imported by domain)
```

### Layer responsibilities

| Layer | Package | Contents | Dependencies |
|---|---|---|---|
| `domain` | `com.atlaspay.{module}.domain` | Aggregate roots, value objects, domain events, repository **ports** (interfaces), domain exceptions | None (pure Java) |
| `application` | `com.atlaspay.{module}.application` | Use case interfaces + implementations, service orchestration, ports-out (interfaces for infrastructure) | `domain` only |
| `infrastructure` | `com.atlaspay.{module}.infrastructure` | JPA entities, repository implementations, external API adapters, Kafka consumers/producers | `application`, `domain` |
| `presentation` | `com.atlaspay.{module}.presentation` | REST controllers, request/response DTOs, OpenAPI annotations | `application` (calls use cases via interfaces) |

### Port examples
```java
// Domain layer — defines WHAT it needs, not HOW
package com.atlaspay.transfers.application.port.out;

public interface TransferGatewayPort {
    TransferReference initiateTransfer(TransferCommand command);
    TransferStatus queryStatus(TransferReference reference);
}

// Infrastructure layer — implements HOW (Anchor-specific)
package com.atlaspay.transfers.infrastructure.adapter.anchor;

@Component
public class AnchorTransferAdapter implements TransferGatewayPort {
    // Anchor HTTP calls here — domain never sees this
}
```

### ArchUnit enforcement
A test in each module verifies the dependency rules:
```java
@Test
void domainLayerHasNoDependencyOnInfrastructure() {
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("..infrastructure..")
        .check(importedClasses);
}
```

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Traditional layered architecture (Controller → Service → Repository) | Repository implementations leak DB concerns into service layer. Hard to test without DB. Provider coupling is implicit. |
| Modular monolith without port interfaces | Faster to write initially but tightly couples domain to Spring Data / Hibernate annotations. Replacing Anchor would require changes deep in business logic. |
| Microservices (separate deployables per bounded context) | Operationally complex for a portfolio project. The hexagonal module structure enables future extraction to microservices without code changes — port interfaces are the seam. |

## Consequences

### Positive
- Domain logic is testable with pure unit tests — no Spring context, no Testcontainers needed
- Swapping Anchor for another BaaS = write a new adapter, domain code unchanged
- Swapping MySQL for another DB = write new repository implementations, domain unchanged
- Clear ownership: infrastructure engineers own adapters; domain engineers own aggregates
- Enables parallel development: teams can work on domain logic while adapters are being built
  (use a stub/WireMock adapter)

### Negative / Trade-offs
- More files/packages per module than a simple layered structure
- Mapping overhead: JPA entities ↔ domain objects requires explicit mapper classes
  (mitigated by `MapStruct` or hand-written static factory methods on aggregates)
- Developers unfamiliar with hexagonal architecture have a learning curve
  → mitigated by this ADR + the domain glossary + consistent module templates

## References
- [Hexagonal Architecture — Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture — Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Get Your Hands Dirty on Clean Architecture — Tom Hombergs](https://leanpub.com/get-your-hands-dirty-on-clean-architecture)
