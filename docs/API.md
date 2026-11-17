# API Contract

All API endpoints are scoped to the authenticated user. Local development uses
HTTP Basic Auth; a production identity-provider adapter is intentionally out of
scope for this project.

## Error format

Validation and domain errors use RFC 9457-style `ProblemDetail` responses. A
conflicting idempotency key and an invalid payment-state transition return
`409 Conflict`; malformed requests return `400 Bad Request`.

## Profile

### `PUT /api/v1/me/profile`

Creates or updates the authenticated user's IANA time zone.

```json
{
  "timeZone": "Asia/Kolkata"
}
```

Every due-date calculation is based on this calendar time zone.

## Commitments

### `POST /api/v1/commitments`

Creates a fixed monthly commitment and its next payment occurrence. The request
requires an `Idempotency-Key` header.

```json
{
  "displayName": "Netflix",
  "category": "SUBSCRIPTION",
  "expectedAmountMinor": 64900,
  "currency": "INR",
  "dueDay": 15,
  "paymentMode": "AUTO_DEBIT"
}
```

The operation atomically writes the commitment, occurrence, audit event,
transactional outbox event, and idempotency record. Repeating the same request
with the same key returns the same resource with `reusedIdempotentResult: true`.

### `GET /api/v1/commitments?month=YYYY-MM`

Returns payment occurrences that fall inside a calendar month. Each result
includes the commitment category, due date, expected amount, payment mode, and
occurrence status.

## Cash forecast

### `GET /api/v1/overview?days=30`

Returns the user’s cash forecast for 1 to 90 calendar days, calculated from
their configured time zone. Paid and skipped occurrences are excluded so the
summary represents expected outgoing money.

```json
{
  "periodStart": "2026-08-09",
  "periodEndExclusive": "2026-09-08",
  "currency": "INR",
  "totalExpectedAmountMinor": 2564900,
  "manualActionAmountMinor": 2500000,
  "autoDebitAmountMinor": 64900,
  "manualActionCount": 1,
  "upcomingPaymentCount": 2,
  "upcomingPayments": [
    {
      "displayName": "Rent",
      "dueDate": "2026-08-10",
      "expectedAmountMinor": 2500000,
      "paymentMode": "MANUAL",
      "occurrenceStatus": "PLANNED",
      "requiresManualAction": true
    }
  ]
}
```

## Payment-occurrence actions

### `POST /api/v1/payment-occurrences/{occurrenceId}/actions`

Applies a business action instead of accepting arbitrary status changes.

```json
{ "action": "MARK_PAID" }
```

```json
{ "action": "SKIP" }
```

```json
{
  "action": "POSTPONE",
  "postponedDueDate": "2026-08-20"
}
```

`POSTPONE` requires a date later than the current due date and preserves the
original due date for auditability. An exact retry of an already-applied action
returns `changed: false` and does not write duplicate audit or outbox events.
