# ADR-001: Use MySQL 8.x as the Primary Database

## Status
Accepted

## Date
2026-08-11

## Context
AtlasPay needs a relational database that:
- Supports ACID transactions for financial integrity
- Has strong ecosystem support in Spring Data JPA / Hibernate
- Has mature tooling in Nigerian fintech infrastructure (RDS, managed cloud offerings)
- Is well-supported by Testcontainers for integration testing
- Supports row-level locking and MVCC for concurrent ledger writes

PostgreSQL and MySQL are both strong candidates. The team has deeper operational
experience with MySQL and the target deployment environment (AWS RDS) has strong
MySQL/Aurora MySQL support with known performance characteristics for our scale.

## Decision
We will use **MySQL 8.x (InnoDB engine)** as the primary relational database across
all AtlasPay modules.

Key configuration:
- Engine: InnoDB (ACID, row-level locking, MVCC, foreign keys)
- Charset: `utf8mb4`, collation `utf8mb4_unicode_ci` (full Unicode + emoji-safe)
- SQL mode: `STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO`
- Monetary columns: `DECIMAL(19,4)` — never `DOUBLE` or `FLOAT`
- Timestamps: `DATETIME(6)` (microsecond precision) with application timezone = UTC
- Schema migrations: Flyway, namespaced per bounded context

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| PostgreSQL 16 | Also excellent. Rejected in favour of MySQL due to team familiarity and target RDS environment. Would revisit if Aurora PostgreSQL becomes the deployment standard. |
| H2 (in-memory) | Acceptable for unit tests only. Rejected as primary DB — too many MySQL-specific behaviours it does not emulate (strict mode, InnoDB locking, `DECIMAL` precision). Testcontainers MySQL is used for integration tests instead. |
| MongoDB | Document model is a poor fit for double-entry ledger and strict relational integrity requirements. |
| CockroachDB | Distributed SQL is over-engineered for current scale. Not needed until horizontal sharding is required. |

## Consequences

### Positive
- Full ACID with InnoDB; row-level locking prevents balance corruption under concurrency
- `DECIMAL(19,4)` enforced at DB level — monetary precision is a DB constraint, not just an app convention
- MySQL `CHECK` constraints (8.0+) allow column-level invariants (e.g., `amount > 0`)
- Excellent Spring Data JPA / Hibernate support; no dialect quirks at this stack version
- Testcontainers `mysql:8.0` image gives real engine in CI — no H2 surprises
- Aurora MySQL available as a drop-in upgrade path if we need managed failover

### Negative / Trade-offs
- MySQL `FULLTEXT` search is less powerful than PostgreSQL `tsvector`; mitigated by using a
  dedicated read model (`atlaspay-transactions-query`) with a targeted `FULLTEXT` index rather
  than general-purpose full-text search
- No native `JSONB` column type (MySQL has `JSON` but without PostgreSQL's indexing depth);
  we store structured data relationally and use `JSON` columns only for raw external webhook payloads
- `ENUM` columns in MySQL are inflexible (altering them requires table rebuild in some cases);
  we use `VARCHAR` + application-level Java `enum` validation instead

## References
- [MySQL 8.0 InnoDB Locking](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html)
- [Hibernate MySQL Dialect](https://docs.jboss.org/hibernate/orm/6.4/userguide/html_single/Hibernate_User_Guide.html)
- [Flyway MySQL support](https://documentation.red-gate.com/fd/mysql-184127604.html)
