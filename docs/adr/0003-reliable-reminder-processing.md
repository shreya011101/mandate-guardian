# ADR 0003: Use a transactional outbox and idempotent worker for reminders

- **Status:** Accepted
- **Date:** 2026-08-08

## Context

Creating a commitment changes durable financial state and must also trigger
future reminder processing. Saving the commitment and then independently
calling a queue or notification provider creates a dual-write failure mode: a
crash between the two operations could save the commitment but lose its
reminder.

The MVP needs reliable asynchronous processing, but does not need Kafka,
RabbitMQ, Redis, or another separately operated broker.

## Decision

When a command changes business state that requires asynchronous work, write
the aggregate change and an `outbox_events` row in the same PostgreSQL
transaction.

The worker polls eligible outbox rows, claims work safely, executes its handler,
and records the result. Initial reminder processing writes a durable
notification job; the first delivery adapter may simply be a development log
or local email sandbox.

The worker must use the following rules:

- claim rows with transaction-safe locking such as `FOR UPDATE SKIP LOCKED`;
- process events at least once, never assuming exactly-once delivery;
- make every consumer idempotent using the event ID or a domain-level
  deduplication key;
- retry transient failures with bounded exponential backoff;
- retain failed rows and expose them for inspection rather than dropping them;
- retain processed events for an initial audit/replay window.

The command API accepts an `Idempotency-Key` header on state-changing requests.
The API stores the key, request fingerprint, response status, and created
resource identity transactionally. A safe retry returns the original result;
a key reused with a different request is rejected.

## Consequences

### Positive

- A commitment and its reminder trigger commit or roll back together.
- Notification failures do not make an HTTP request slow or unreliable.
- The worker can be restarted and retried without duplicate reminders.
- The design demonstrates a production-relevant reliability pattern without
  operating a separate message broker.

### Negative

- Reminder delivery is eventually consistent rather than synchronous.
- The outbox table needs indexes, monitoring, and a retention job as volume
  grows.
- Idempotent processing and retry logic take more discipline than a simple
  `@Async` method.

## Rejected alternatives

### Calling a notification provider directly in the HTTP request

Rejected because provider latency or failure would affect commitment creation,
and a crash after the database commit could lose the reminder.

### Spring `@Async` or an in-memory queue as the source of truth

Rejected because queued work would disappear on process restart and could not
be audited or retried reliably.

### Kafka or RabbitMQ in the MVP

Rejected because a dedicated broker is unnecessary operational overhead for the
initial product. An outbox relay can publish to a broker later if independent
services or throughput justify it.
