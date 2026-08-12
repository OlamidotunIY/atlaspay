# Module Design Document — `atlaspay-fraud`

> **Status:** `APPROVED`
> **Author:** Antigravity & User
> **Created:** 2026-08-12
> **Last Updated:** 2026-08-12

---

## 1. Overview
The `atlaspay-fraud` module acts as the system's risk and compliance engine. It consumes transaction events and evaluates them against specific, algorithmically rigorous rules. Rather than simple single-violation blocklists, it uses additive event-sourced weighted scoring, Redis sliding windows, online statistical algorithms, and graph traversal.

---

## 2. Folder Structure

```text
atlaspay-fraud/
└── src/main/java/com/atlaspay/fraud/
    ├── domain/
    │   ├── model/
    │   │   ├── FraudCase.java                   # Aggregate Root
    │   │   ├── InvestigationStatus.java         # Enum
    │   │   └── RiskScore.java                   # Value Object (Record)
    │   ├── event/
    │   │   ├── VelocityRuleTriggered.java       # Record (Explainability event)
    │   │   ├── VolumeSpikeDetected.java         # Record (Explainability event)
    │   │   ├── CyclicMovementDetected.java      # Record (Explainability event)
    │   │   └── MerchantFraudFlagged.java        # Record (Trigger identity ban)
    │   └── repository/
    │       └── FraudCaseDomainRepository.java
    ├── application/
    │   ├── command/
    │   │   ├── EvaluateTransactionCommand.java  # Internal command from Kafka listener
    │   │   └── ResolveFraudCaseCommand.java     # Admin action
    │   ├── query/
    │   │   └── GetOpenFraudCasesQuery.java      # Admin query
    │   ├── dto/
    │   │   ├── FraudCaseDto.java
    │   │   └── FraudExplanationDto.java
    │   ├── usecase/
    │   │   ├── TransactionEvaluationUseCase.java
    │   │   └── ResolveFraudCaseUseCase.java
    │   └── rules/
    │       ├── FraudRule.java                   # Interface for all rules
    │       ├── VelocityRuleEvaluator.java       # Redis logic
    │       ├── VolumeSpikeRuleEvaluator.java    # Welford's algorithm
    │       └── CyclicMovementRuleEvaluator.java # DFS Graph traversal
    ├── infrastructure/
    │   ├── persistence/
    │   │   ├── FraudCaseEntity.java             # JPA Entity
    │   │   ├── FraudExplanationEntity.java      # Child JPA Entity
    │   │   └── WelfordStatsEntity.java          # Stores running mean/variance
    │   ├── adapter/
    │   │   └── redis/
    │   │       └── RedisVelocityAdapter.java    # Implementation of Sliding Window Log
    │   └── messaging/
    │       ├── FraudEventPublisher.java         # Kafka Outbox publisher
    │       └── TransactionEventListener.java    # Listens to TransferInitiated, ChargeSuccessful
    └── presentation/rest/
        └── AdminFraudController.java            # Admin Dashboard endpoints
```

---

## 3. Core Algorithms & Java 21 Implementation

### 3.1 Rule A: Velocity / Structuring (AML "Smurfing")
**Trigger:** Many small transactions in a short window.
**Algorithm:** **Redis Sliding-Window Log**.
We strictly avoid DB `COUNT()` queries. `VelocityRuleEvaluator` uses Spring Data Redis `RedisTemplate`:

```java
// Simplified pseudo-implementation
long now = Instant.now().toEpochMilli();
long windowStart = now - Duration.ofMinutes(5).toMillis();
String key = "velocity:" + merchantId;

// Pipeline operations to minimize RTT
redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
    byte[] rawKey = key.getBytes();
    connection.zSetCommands().zAdd(rawKey, now, transactionId.getBytes());
    connection.zSetCommands().zRemRangeByScore(rawKey, "-inf", String.valueOf(windowStart));
    connection.keyCommands().expire(rawKey, 300); // 5 min TTL
    return null;
});

Long count = redisTemplate.opsForZSet().zCard(key);
if (count > 10) return new VelocityRuleTriggered(merchantId, 50, count);
```

### 3.2 Rule B: Volume Spike
**Trigger:** A single transaction that deviates sharply from a merchant's historical baseline.
**Algorithm:** **Welford's Online Algorithm (Incremental Mean/Variance)**.
Instead of querying all historical transactions to calculate standard deviation, `WelfordStatsEntity` stores `count`, `mean`, and `M2` (sum of squares of differences).

```java
// Pseudo-code in VolumeSpikeRuleEvaluator
double delta = amount - stats.getMean();
stats.setMean(stats.getMean() + delta / stats.getCount());
double delta2 = amount - stats.getMean();
stats.setM2(stats.getM2() + delta * delta2);

double variance = stats.getM2() / (stats.getCount() - 1);
double stdDev = Math.sqrt(variance);
double zScore = Math.abs(amount - stats.getMean()) / stdDev;

if (zScore > 3.0) return new VolumeSpikeDetected(merchantId, 30, zScore);
```

### 3.3 Rule C: Cyclic Movement
**Trigger:** Funds looping (A → B → A).
**Algorithm:** **In-Memory Adjacency-List DFS**.
`CyclicMovementRuleEvaluator` pulls the last 3-4 hops from a materialized read-model and builds a lightweight graph in memory.
```java
public boolean hasCycle(String startAccount, Map<String, List<String>> graph, int maxDepth) {
    // Standard DFS utilizing Java Collections (Set/Deque)
}
```

---

## 4. Additive Scoring & Event-Sourced Explanations

**Design:** Rather than a binary "flag/no-flag", we use weighted scores.
*   `VelocityRuleTriggered` → +50 points
*   `VolumeSpikeDetected` → +30 points
*   `CyclicMovementDetected` → +60 points

The `TransactionEvaluationUseCase` runs all rules using Java 21 `CompletableFuture.allOf()` to execute them in parallel virtual threads. It collects the triggered events.
If `SUM(points) >= 30`:
1.  Create `FraudCase`.
2.  Save the explanation events as child entities (`FraudExplanationEntity`) to give investigators precise reasoning.
3.  If `SUM(points) >= 70`, immediately emit `MerchantFraudFlagged` (via Outbox) which `atlaspay-identity` listens to in order to apply an auto-hold.

---

## 5. REST API & DTOs

**GET** `/api/v1/admin/fraud-cases/{merchantId}`
**Response DTO:**
```json
{
  "merchantId": "mer_123",
  "status": "OPEN",
  "totalScore": 80,
  "explanations": [
    {
      "rule": "VELOCITY",
      "weight": 50,
      "details": "12 transactions within 5 minutes"
    },
    {
      "rule": "VOLUME_SPIKE",
      "weight": 30,
      "details": "Amount deviated by 3.2 sigma from mean"
    }
  ]
}
```

---

## 6. Database (MySQL 8)

Flyway: `V1__fraud__001_create_schema.sql`

```sql
CREATE TABLE fraud_cases (
    id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_score INT NOT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE fraud_explanations (
    id VARCHAR(50) PRIMARY KEY,
    case_id VARCHAR(50) NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    weight INT NOT NULL,
    details JSON NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_fraud_case FOREIGN KEY (case_id) REFERENCES fraud_cases(id) ON DELETE CASCADE
);

CREATE TABLE merchant_welford_stats (
    merchant_id VARCHAR(50) PRIMARY KEY,
    txn_count BIGINT NOT NULL DEFAULT 0,
    mean DOUBLE NOT NULL DEFAULT 0.0,
    m2 DOUBLE NOT NULL DEFAULT 0.0,
    updated_at DATETIME(6) NOT NULL
);
```
