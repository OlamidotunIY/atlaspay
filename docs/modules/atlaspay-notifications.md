# Module Design Document — `atlaspay-notifications`

> **Status:** `APPROVED`
> **Author:** Antigravity & User
> **Created:** 2026-08-13
> **Last Updated:** 2026-08-13

---

## 1. Overview
The `atlaspay-notifications` module is responsible for asynchronous outbound communication to clients and merchants. Currently, it supports Email Notifications and Real-time WebSocket (STOMP) Notifications. It is fully decoupled from other modules and purely event-driven, relying on Kafka to consume events (like `VirtualAccountActivatedEvent` or `MerchantRegistered`) and dispatch them.

---

## 2. WebSocket Configuration (STOMP)
The system uses Spring Boot's WebSocket STOMP messaging protocol.

### 2.1 Connection Details
*   **Endpoint:** `ws://<host>:<port>/ws`
*   **Protocol:** STOMP over WebSocket (with SockJS fallback available).
*   **Authentication:** The WebSocket handshakes do not natively support headers easily in all browsers. Therefore, authentication is performed via a **STOMP Channel Interceptor**. Clients MUST send their JWT Token in the STOMP `CONNECT` frame headers as `Authorization: Bearer <token>`.
*   **Security:** The interceptor extracts the `merchantId` (`integration`) from the JWT and binds an `AtlasPayAuthenticationToken` to the WebSocket session, mapping the connection to a specific user.

### 2.2 Subscribable Channels
Merchants can subscribe to user-specific private queues to receive real-time updates.

*   **Notifications Queue:** `/user/queue/notifications`
    *   *Usage:* Real-time updates regarding account issuance, transaction success, compliance status changes, etc.

---

## 3. Event Handling Architecture
The module listens to Kafka topics published by the Outbox Pattern in other modules.

1.  `account-events` topic:
    *   Listens for `VirtualAccountActivatedEvent`.
    *   Parses the event to a local DTO (`VirtualAccountActivatedNotificationEvent`).
    *   Uses `SimpMessagingTemplate.convertAndSendToUser(merchantId, "/queue/notifications", event)` to push the event to the merchant.
2.  `merchant-events` topic:
    *   Listens for `MerchantRegistered` and `MerchantEmailVerificationResent`.
    *   Dispatches SMTP verification emails via `SendVerificationEmailUseCase`.

---

## 4. Simulator Callback Flow
The `atlaspay-simulator` module acts as a mock BaaS provider. When it completes an asynchronous action (e.g., assigning a NUBAN), it triggers an HTTP POST webhook to `atlaspay-accounts`. 
The `atlaspay-accounts` module processes this webhook and publishes a `VirtualAccountActivatedEvent` to Kafka. 
The `atlaspay-notifications` module picks this up and pushes the final NUBAN directly to the Merchant's WebSocket connection in real time, completing the loop.
