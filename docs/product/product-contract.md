# Mandate Guardian — Product Contract (v0.1)

## Product statement

Mandate Guardian is a personal recurring-commitments control tower. It gives a
user one trustworthy place to see money expected to leave their account, know
which payments need action, and receive reminders before a due date is missed.

It is not a payment app. It records, forecasts, and helps a user act on their
own financial commitments; it never holds funds, initiates a transfer, or asks
for banking credentials.

## Initial user

A salaried Indian customer who manages several recurring commitments across
different providers: rent, loan EMIs, credit-card bills, furniture rentals, and
digital subscriptions. They currently depend on separate apps, vague payment
descriptors, and email receipts to remember what is due.

## Job to be done

> When I am planning the month, help me understand every upcoming financial
> commitment and remind me in time, so I can reserve money and avoid a missed
> payment.

## MVP promise

In under two minutes, a user can add their important recurring commitments and
see the total amount due in the next 30 days, grouped by:

- automatically debited payments;
- payments that need manual action.

The system reliably creates each expected payment occurrence and sends the
user's configured reminder before it becomes due.

## MVP user journey

1. A user creates a commitment, for example: `Netflix, INR 649, monthly, 15th,
   auto-debit`.
2. Mandate Guardian validates and stores the commitment.
3. It creates the next expected payment occurrence in the user's time zone.
4. It schedules reminder work according to the user's reminder rule.
5. The user sees the occurrence in the upcoming-payments view and can later
   mark it paid, skipped, or postponed.

The first backend vertical slice ends after step 4. A browser or mobile UI is
not required to prove the workflow; documented REST endpoints and automated
tests are the initial client.

## Ubiquitous language

| Term | Meaning |
| --- | --- |
| **Commitment** | The continuing obligation itself, such as a rent agreement or Netflix subscription. |
| **Payment occurrence** | One expected payment created from a commitment, such as Netflix due on 15 August. |
| **Payment record** | Evidence that money was paid: a manually entered reference, receipt, or later an imported transaction. |
| **Capture** | Raw input that may produce a payment record or a draft commitment, such as a shared receipt or email. |
| **Reminder rule** | The user's preferred timing and channel for reminders. |
| **Merchant alias** | A user-confirmed mapping from an unclear descriptor (for example, `APPLE.COM/BILL`) to a recognisable name. |
| **Auto-debit** | The payment is expected to be collected automatically; the user still needs visibility, but normally no manual payment action. |
| **Manual payment** | The user must make or confirm the payment themselves before the due date. |

## Scope of the first release

### Included

- One authenticated user and their own commitments.
- Manual creation, viewing, editing, pausing, and ending of commitments.
- Fixed-amount monthly commitments in INR.
- Manual or auto-debit payment mode.
- Next payment-occurrence generation.
- Configurable pre-due reminder rule.
- Marking an occurrence paid, skipped, or postponed.
- A 30-day total of expected commitments.
- A durable audit trail for user-initiated changes and scheduled reminders.

### Explicitly deferred

- Moving money, initiating UPI/card payments, or holding payment credentials.
- Bank, Account Aggregator, UPI, Apple, Netflix, Gmail, or Outlook integrations.
- Receipt OCR, email parsing, and merchant-alias discovery.
- Variable credit-card bill amounts and a full cash-balance forecast.
- Shared household accounts, delegated access, and bill splitting.
- Financial advice, loan-prepayment recommendations, or tax advice.
- A native mobile app and push notifications.

## Product rules

1. A commitment and a payment record are separate. A user can plan rent before
   there is proof that it was paid.
2. An occurrence belongs to exactly one commitment and represents one expected
   due date.
3. A payment record can confirm an occurrence, but must not overwrite its
   original expected amount or due date.
4. A monthly due day may be from 1 to 31. When a month has no such day, the
   occurrence is due on that month's final calendar day.
5. All due-date calculations use the user's configured IANA time zone. A due
   date is a local calendar date; it is not a UTC timestamp.
6. Amounts are stored as integer minor units plus ISO currency code. The MVP
   accepts INR only, but the data model remains currency-aware.
7. The system must never infer that an occurrence is paid without an explicit
   user action or a future confirmed payment record.

## Initial states

### Commitment

`ACTIVE` → `PAUSED` → `ACTIVE` or `ENDED`

An ended commitment creates no future occurrences. A paused commitment retains
its history but creates no new occurrences until it is resumed.

### Payment occurrence

`PLANNED` → `DUE | PAID | SKIPPED | POSTPONED`

`DUE` → `PAID | SKIPPED | POSTPONED | OVERDUE`

`POSTPONED` represents a deliberate user action and retains the original due
date for auditability. The detailed rescheduling rules will be decided before
implementation.

## First vertical-slice acceptance criteria

The foundation slice is complete when all of the following are true:

1. An authenticated API client can create a fixed monthly commitment.
2. The API validates amount, currency, cadence, due day, payment mode, and
   time zone.
3. The system persists the commitment and exactly one next payment occurrence
   in a single database transaction.
4. It writes an outbox event in that same transaction for the reminder worker.
5. Repeating the request with the same idempotency key does not create a second
   commitment or occurrence.
6. Unit tests cover monthly-date edge cases, including the 29th, 30th, and
   31st.
7. Integration tests run against PostgreSQL rather than an in-memory database.

## Success measure for the MVP

For a seeded demo user with ten commitments, the API can correctly return every
expected occurrence in the following 30 days and the worker produces one
deduplicated reminder job for each applicable reminder rule.

## Open decisions, intentionally postponed

- Exact authentication provider and session/token model.
- Email versus push notification as the first real delivery channel.
- Web frontend technology.
- The database schema and REST resource shapes.
- Whether an occurrence can be partially paid.
