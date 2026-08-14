# 13. Centralized Internal Simulator Communication via Webhooks/REST

Date: 2026-08-14

## Status
Accepted

## Context
Our application needs to mock a third-party Banking-as-a-Service (BaaS) provider using an internal module (`atlaspay-simulator`). When creating a dedicated virtual account, the accounts module (`atlaspay-accounts`) must request a NUBAN from the provider, which is generated asynchronously and returned via a webhook. 

While internal modules typically communicate using domain events (via Kafka/Outbox), treating the simulator as just another internal module would bypass our HTTP networking boundary. We need to accurately test the robustness of our webhook reception, HTTP timeouts, and exponential backoff mechanisms before plugging in a real provider.

Additionally, as more modules (like `atlaspay-transfers`) begin communicating with the simulator, scattering `RestClient` instantiations across adapters leads to duplication and hard-to-maintain URL configurations.

## Decision
1. **HTTP over Internal Events:** We will force the `atlaspay-simulator` to communicate with the rest of the application strictly via HTTP REST and webhooks, despite running in the same JVM. 
2. **Centralized Configuration:** We will define a single `@Configuration` bean (`SimulatorConfig`) in the root `atlaspay-app` module that builds a `RestClient` configured with the simulator's base URL and properties.
3. **Dependency Injection:** Any outbound adapter requiring communication with the simulator (e.g., `SimulatorAccountAdapter`) will inject this centralized `@Qualifier("simulatorRestClient") RestClient`.

## Consequences
- **Positive:** We thoroughly battle-test our webhook ingestion controllers (e.g., `SimulatorWebhookController`) and HTTP handling mechanisms, guaranteeing they work exactly as they will in production.
- **Positive:** Reduces boilerplate across adapters by providing a pre-configured HTTP client.
- **Negative:** Adds slight network overhead to local development compared to in-memory events, though negligible for testing purposes.
