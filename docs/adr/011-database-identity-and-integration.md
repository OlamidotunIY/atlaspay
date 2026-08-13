# ADR-011: Database Auto-Increment IDs and Integration Terminology

## Status
Accepted

## Date
2026-08-13

## Context
When designing the identity architecture across multiple bounded contexts (Identity, Accounts, etc.), we needed to determine the best approach for primary keys, referencing related entities, and exposing IDs to public API consumers.

Historically, we considered using UUIDs everywhere for primary keys to prevent enumeration attacks and ensure global uniqueness. However, this has a significant performance penalty on relational databases like MySQL (due to InnoDB's clustered index fragmentation).

Additionally, when child entities like `Customer` and `VirtualAccount` refer back to the B2B Merchant that owns them, using the term `merchantId` inside those boundaries can cause cognitive overload, as it mixes the specific B2B identity domain with generic module resources. 

Finally, we needed to decide which resources required external "Reference Codes" (e.g. `CUS_uuid`) versus which resources could simply be referenced by their internal database IDs in the API.

## Decision

1. **Database Identifiers**: We will use SQL Auto-Increment `BIGINT` (`Long` in Java) for **all** database primary keys across all tables, rather than random UUIDs. This optimizes database indexing and foreign key relationships.
2. **Integration Terminology**: The foreign key linking an entity to its owning Merchant will be named `integration` (storing the `Long` ID of the merchant). This genericizes the terminology within bounded contexts, indicating "the B2B platform integrating with our API".
3. **Reference Codes**: We will **only** issue external string reference codes for the `Customer` entity (in the format `CUS_<randomUUID>`). Other entities (Merchant, VirtualAccount) will simply use their internal `Long` IDs (or UUIDs where specifically requested by other constraints).

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| UUID v4 for all Primary Keys | Causes severe index fragmentation in MySQL InnoDB, slowing down inserts and range queries over time. |
| Exposing Reference Codes for all entities | Unnecessary complexity for our specific B2B use case where only certain resources (like Customers) require obfuscated or highly decoupled external references. |
| Using `merchantId` everywhere | Couples the domain language of every bounded context explicitly to the Identity module's `Merchant` concept, rather than focusing on the API integration aspect. |

## Consequences

### Positive
- Massive performance improvements for MySQL `INSERT` operations due to sequential `BIGINT` clustering.
- Cleaner ubiquitous language: `integration` clearly implies the B2B tenant making the API call.
- Reduced complexity: Only generating obfuscated reference codes where explicitly mandated (Customers).

### Negative / Trade-offs
- The API boundary must be careful to properly map `integration` in payloads/DB to the correct internal Merchant ID.
- Using auto-increment IDs for some public-facing resources could expose system scale (e.g., seeing Virtual Account ID `105` tells a user we have at least 105 accounts). 

## References
- ADR-001 (MySQL over PostgreSQL)
- AtlasPay Domain Glossary (`Integration` and `Reference Code` definitions)
