# ADR-012: WebSocket Push Notifications Architecture

## Status
Accepted

## Date
2026-08-13

## Context
As the AtlasPay application grows, we require a mechanism to push real-time updates to merchants (such as Virtual Account activation, transaction completions, and compliance approvals). Relying purely on HTTP polling by the frontend is inefficient and scales poorly. We needed to introduce WebSockets.

The key considerations were:
1. Which module should handle WebSockets to prevent coupling?
2. How do we authenticate WebSocket connections in a stateless JWT architecture?
3. What protocol should we use over WebSockets?

## Decision

1. **Dedicated Module & Event-Driven Topology:** We will use the existing `atlaspay-notifications` module as the sole orchestrator of outbound WebSockets. Rather than other modules tightly coupling themselves to a "WebSocket Push Service", they will continue to emit standard Domain Events (via the Outbox Pattern to Kafka). The `atlaspay-notifications` module will consume these events and push them down the socket.
2. **STOMP Protocol:** We will use Spring WebSocket with the STOMP (Simple Text Oriented Messaging Protocol) sub-protocol. This gives us built-in routing (topics vs queues) and seamless integration with Spring Security (`@SendToUser`).
3. **Authentication via STOMP CONNECT:** Standard browser WebSockets do not support custom HTTP headers during the handshake. We will implement a `ChannelInterceptor` (`JwtChannelInterceptor`) to intercept the STOMP `CONNECT` frame, extract the `Authorization` header containing the JWT, validate it, and attach an `AtlasPayAuthenticationToken` to the session.

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Server-Sent Events (SSE) | Unidirectional (server-to-client only). While sufficient for simple notifications, WebSockets offer bi-directional capabilities if we ever need the client to acknowledge messages or send quick commands without full HTTP overhead. |
| Raw WebSockets (No STOMP) | We would have to manually build our own routing logic, user-session mapping, and JSON parsing. STOMP handles all of this out of the box in Spring Boot. |
| Passing JWT in query param during HTTP Handshake | Passing tokens in URLs (e.g., `ws://...?token=xyz`) is a massive security risk, as URLs are logged in access logs, proxies, and browser history. |

## Consequences

### Positive
*   Complete decoupling: Core domain modules (`accounts`, `identity`) remain entirely unaware of WebSockets.
*   Highly secure: JWTs are passed safely in the STOMP frame body/headers, avoiding URL leakage.
*   Scalable: We can easily add RabbitMQ or ActiveMQ as a full external STOMP broker later if we outgrow the simple in-memory broker.

### Negative / Trade-offs
*   Slightly increased frontend complexity (clients must use a STOMP library like `@stomp/stompjs` rather than the native raw `WebSocket` API).
