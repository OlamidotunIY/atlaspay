# AtlasPay Architecture & Coding Rules

This project uses Hexagonal Architecture (Ports and Adapters) combined with Domain-Driven Design (DDD) and CQRS. You MUST follow these rules strictly.

## 1. CQRS and Application Layer Structure
- **Commands**: DTOs that mutate state MUST be suffixed with `Command` (e.g., `CreateCustomerCommand`) and placed in `application/command`.
- **Queries**: DTOs that read state MUST be suffixed with `Query` (e.g., `GetCustomerQuery`) and placed in `application/query`.
- **Responses**: DTOs returned by use cases MUST be placed in `application/dto`.
- **Use Cases**: All handlers MUST be placed in `application/usecase` and MUST extend `BaseUseCase<Input, Output>` from `atlaspay-shared-kernel`.
  - For commands that do not return a result, extend `BaseUseCase<Input, Void>` and return `null`. Do NOT create or use a separate `BaseCommandUseCase`.

## 2. Hexagonal Architecture (Ports and Adapters)
- **Domain Layer (`domain`)**: Contains Aggregate Roots, Value Objects, Domain Events, Domain Exceptions, and **Repository Interfaces**.
  - Repositories are in `domain/repository`. They define *what* the domain needs to persist state.
- **Application Layer (`application`)**: Orchestrates use cases. Contains Commands, Queries, DTOs, Use Cases, and **Outbound Ports (`port/out`)**.
  - Query Services (e.g., `CustomerQueryService`) and external service interfaces (e.g., `AccountNameResolutionPort`) live in `application/port/out`.
  - Do NOT create `port.in` interfaces. The `BaseUseCase<I, O>` class effectively serves as the inbound port for the adapters.

## 3. Domain-Driven Design (DDD) Rules
- **Aggregates**:
  - MUST NOT have public setters. State changes must happen via explicit business methods (e.g., `deactivate()`, `verifyEmail()`).
  - Should expose necessary public getters for validation by use cases.
  - Must queue domain events internally (typically handled by a `BaseAggregateRoot` class).
- **Domain Events**:
  - Must be immutable (`record`) and implement the `DomainEvent` interface.
  - Must be wrapped in `EnvelopedDomainEvent` before publishing (to attach `correlationId` and `dispatchedAt`).
  - Do NOT write boilerplate code to pull, envelop, and publish events in individual use cases. `BaseUseCase` has a protected `publishEvents(aggregate, publisher)` method that does this automatically. Simply call this method after saving the aggregate.

## 4. Exceptions and Error Handling
- Use the unified module `ErrorCode` enum (e.g., `IdentityErrorCode`).
- Throw custom business exceptions (`BusinessRuleException`, `NotFoundException`, `ConflictException`, `ValidationException`) from the shared kernel, passing the `ErrorCode`.
- Do NOT throw generic `RuntimeException` or `IllegalArgumentException` for business errors.

## 5. Documentation
- ANY architectural changes, new use cases, new endpoints, or renamed concepts (e.g., `KycStatus` to `ComplianceStatus`) MUST be immediately reflected in the documentation (`docs/design.md`, `docs/modules/*.md`, `docs/domain-glossary.md`).
- Ensure folder structure changes are documented in `docs/design.md` and `docs/module-design-template.md`.

## 6. Git Commits
- Commit changes in small, logical, atomic chunks. Do not lump massive refactors and new features into a single commit.

## 7. Code Style
- **NO INLINE IMPORTS**: You must NEVER use inline imports in Java files (e.g., `java.util.Map<...>`). All imports MUST be placed at the top of the file. NEVER FORGET THIS RULE.

## 8. Entities and Mappers
- **JPA Entities**: MUST have their own explicit constructors (or Lombok `@AllArgsConstructor` / `@NoArgsConstructor` where strictly required by JPA). ONLY fields that can be legitimately updated should have a `@Setter`. Do NOT put `@Setter` at the class level unless every single field is mutable.
- **Mappers**: The `infrastructure` layer must contain a `mapper` package. For every Entity/Aggregate, you must define a specific Mapper class responsible for converting between Domain and JPA Entity. Adapters MUST use these mapper classes rather than mapping inline.
- **Mappers & Domain Events**: Domain events must ONLY be pulled from the application layer (e.g. inside Use Cases). You MUST NEVER call `.pullDomainEvents()` inside the Mappers. Mappers should ONLY map data. To safely map database entities into Domain Aggregates without triggering business events, implement a "Reconstitution Constructor" or static factory in the aggregate specifically for the Mapper to use.
