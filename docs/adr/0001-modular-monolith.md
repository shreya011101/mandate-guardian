# ADR 0001: Build Mandate Guardian as a modular monolith

- **Status:** Accepted
- **Date:** 2026-08-08

## Context

Mandate Guardian is being developed and operated by one person. Its MVP needs
strong financial-data consistency, a reliable reminder workflow, and a simple
local and production setup. It does not yet have independently scaling teams,
unequal traffic profiles, or external consumers that would justify distributed
services.

The code must nevertheless have boundaries that make its business rules easy to
reason about and allow a future extraction if that becomes justified.

## Decision

Build one Java 21 / Spring Boot application in one Maven repository with one
PostgreSQL database. Structure it as a modular monolith:

```text
com.mandateguardian
  identity/
  commitments/
  occurrences/
  notifications/
  captures/
  audit/
  shared/
```

Each business module owns its `api`, `application`, `domain`, and
`infrastructure` packages. A module may expose an explicit application service
or publish a domain event; it must not reach into another module's repository
or persistence entity.

The API service and the background worker share the same codebase and database
schema. They run as separate Spring profiles/processes in production and may
run together during local development. This keeps the deployment simple while
preventing slow notification work from blocking an HTTP request.

The primary external interface is versioned REST with OpenAPI documentation.
The initial client is an API test suite; a frontend is not a dependency of the
backend design.

## Consequences

### Positive

- One repository, one deployment model, and one transactional database make
  local development and debugging straightforward.
- Module boundaries preserve a domain-oriented design without network calls or
  distributed transactions.
- The worker can be deployed or scaled separately when notification volume
  requires it.
- Future extraction has a natural boundary: a module's application API and
  durable events.

### Negative

- All modules share the same release cadence and database instance.
- Package boundaries are a team discipline and must be protected by code
  reviews and architecture tests.
- A future service extraction will require a deliberate data-ownership plan;
  this ADR does not promise that it will be easy or necessary.

## Rejected alternatives

### Microservices

Rejected for the MVP. They would add service discovery, distributed tracing,
network failure modes, deployment overhead, and data-consistency complexity
before the product has traffic or independent teams that need them.

### A single controller/service/repository layer for the entire application

Rejected because it makes commitment, occurrence, and notification rules
entangle quickly. The project should demonstrate meaningful domain boundaries,
not merely CRUD endpoints.

### Event sourcing

Rejected for now. An append-only audit log provides the needed traceability
without making projections, event-versioning, and replay the primary storage
model. It can be reconsidered only if the product requires full historical
state reconstruction.
