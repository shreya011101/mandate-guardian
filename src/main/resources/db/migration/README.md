# Database migrations

Flyway migrations live in this directory and use the form
`V<version>__<description>.sql`.

The first real migration will create the users, commitments, payment
occurrences, idempotency, audit, and outbox tables together with their
constraints. No placeholder migration is needed: the schema must begin with a
meaningful, reviewable change.
