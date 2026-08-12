# ADR-005: Use Java 21 Virtual Threads for All Blocking I/O

## Status
Accepted

## Date
2026-08-11

## Context
AtlasPay is I/O-bound: most request processing involves waiting on MySQL queries,
Kafka produce/consume, HTTP calls to Anchor/Dojah, or Redis reads. Under the traditional
platform thread model:

- Each blocked thread holds a 512KB–1MB OS stack
- A thread pool of 200 threads consumes ~200MB just for stacks, not counting heap
- Under high concurrency, threads queue up waiting for I/O — throughput is limited by thread count,
  not by CPU

Java 21 introduces **virtual threads** (Project Loom) — lightweight JVM-managed threads.
Millions can exist simultaneously because they don't hold OS threads while blocked on I/O.
The JVM unmounts a virtual thread from its carrier OS thread the moment it blocks,
reusing that carrier for other work.

## Decision
All blocking I/O paths in AtlasPay run on **virtual threads**:

1. **HTTP request handling**: Spring Boot 3.2+ Tomcat configured with virtual thread executor
   ```yaml
   spring:
     threads:
       virtual:
         enabled: true
   ```
2. **`@Async` beans**: `AsyncConfigurer` returns `Executors.newVirtualThreadPerTaskExecutor()`
3. **Scheduled tasks** (`BillingCycleScheduler`, `SettlementBatchScheduler`, `OutboxPoller`):
   submit work via `Executors.newVirtualThreadPerTaskExecutor()`
4. **Outbound HTTP clients** (`RestClient` to Anchor, Dojah): called from virtual thread context
5. **JDBC**: HikariCP pool accessed from virtual threads — connection acquisition blocks the virtual
   thread, not an OS thread

### What does NOT use virtual threads
- **CPU-bound work** (e.g., BCrypt password hashing, JWT signature verification):
  bounded `ThreadPoolExecutor` sized to `availableProcessors()` — virtual threads would
  occupy carrier threads without yielding and provide no benefit
- **Structured concurrency** (`StructuredTaskScope`): uses virtual threads internally
  but is expressed through the structured API, not raw `Thread` creation

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Traditional platform thread pool (Tomcat default) | Limited concurrency under I/O load; higher memory footprint. Replaced by virtual threads at Java 21. |
| Reactive programming (Project Reactor / WebFlux) | Also solves I/O concurrency, but requires pervasive async/non-blocking API throughout the codebase. Steep learning curve; harder to debug (no stack traces across operators); incompatible with blocking JDBC. Virtual threads achieve the same concurrency with imperative, synchronous code. |
| Kotlin coroutines | Excellent but introduces a Kotlin dependency into a Java-only codebase. |

## Consequences

### Positive
- Throughput scales with I/O concurrency, not thread pool size
- Code remains synchronous and readable — no `Mono<>`, no `CompletableFuture<>` chains for routine I/O
- `CompletableFuture` still used selectively for deliberate parallelism (e.g., parallel verification calls)
  but not forced on all code
- Debuggability: stack traces are normal; no async operator chains to untangle
- Spring Boot 3.2+ makes this a one-line config change — zero code changes in business logic

### Negative / Trade-offs
- **Pinning**: virtual threads pin their carrier OS thread when blocked inside a `synchronized`
  block or native method. Pinning under load degrades to platform thread behaviour.
  Mitigation: replace `synchronized` with `ReentrantLock` in any code that does I/O inside a lock.
  Spring Boot 3.2+ logs pinning events at startup.
- HikariCP pool size still limits DB concurrency (virtual threads don't remove the need for
  connection pooling — the DB has finite connections). Pool size must be tuned separately.
- Virtual threads are not a silver bullet for CPU-bound work — using them for BCrypt would
  starve carrier threads.

## References
- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
- [Spring Boot 3.2 Virtual Thread Support](https://spring.io/blog/2023/11/23/spring-boot-3-2-0-available-now#virtual-threads)
- [Project Loom — Inside Java](https://inside.java/2023/10/19/virtual-threads-101/)
