# Mandate Guardian

Mandate Guardian is a personal recurring-commitments control tower. It helps a
user understand recurring financial obligations, know which payments need
manual action, and receive reliable reminders before due dates.

The initial backend is a Java 21 / Spring Boot modular monolith using
PostgreSQL, Flyway, Testcontainers, Docker Compose, and GitHub Actions.

## What is in scope

The first vertical slice creates a fixed monthly commitment, generates its next
payment occurrence in the user's time zone, and writes a durable reminder
event. See the [product contract](docs/product/product-contract.md) and the
[architecture decisions](docs/adr/) for the full rationale.

## Prerequisites

- Java 21 (Eclipse Temurin is recommended)
- Docker Desktop running locally
- Git

Maven does not need to be installed. The checked-in Maven Wrapper downloads and
uses the required Maven distribution.

## Run locally

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

The application starts with the `local` profile by default. Confirm it is ready:

```bash
curl http://localhost:8080/actuator/health
```

The defaults are intentionally usable as-is. To customise the database name,
port, username, or password, copy `.env.example` to `.env`, then export the
values before starting Spring Boot:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

Stop the database while retaining its data:

```bash
docker compose down
```

`docker compose down -v` also removes the local Postgres volume and is a full
local-data reset.

## Test

```bash
./mvnw verify
```

Integration tests use Testcontainers to launch PostgreSQL in Docker. This means
tests exercise the same database family as local development, rather than an
in-memory substitute.

## Exercise the first API slice

Local development uses HTTP Basic Auth only as a development adapter. The
default credentials are `local-developer` and `local-development-only`; change
them with `LOCAL_SECURITY_USERNAME` and `LOCAL_SECURITY_PASSWORD` before
starting the application.

First create the authenticated user's profile and time zone:

```bash
curl --user local-developer:local-development-only \
  --request PUT http://localhost:8080/api/v1/me/profile \
  --header 'Content-Type: application/json' \
  --data '{"timeZone":"Asia/Kolkata"}'
```

Then create a commitment. Amounts are in minor units, so `64900` means INR
649.00:

```bash
curl --include --user local-developer:local-development-only \
  --request POST http://localhost:8080/api/v1/commitments \
  --header 'Content-Type: application/json' \
  --header 'Idempotency-Key: netflix-2026-08-08' \
  --data '{
    "displayName":"Netflix",
    "category":"SUBSCRIPTION",
    "expectedAmountMinor":64900,
    "currency":"INR",
    "dueDay":15,
    "paymentMode":"AUTO_DEBIT"
  }'
```

Sending the same request with the same idempotency key returns the original
commitment rather than creating another one. Non-local profiles deny all API
requests until a production identity-provider adapter is added.

## Repository conventions

- Schema changes are Flyway migrations under `src/main/resources/db/migration`.
- Monetary values use integer minor units plus ISO currency code.
- Due dates are local dates; audit timestamps are UTC instants.
- State-changing API requests use idempotency keys.
- Background work is initiated through a PostgreSQL transactional outbox.
- No banking credentials or payment initiation belong in this application.
