# ADR 0001 — Database per service

**Status:** accepted

## Context

Four services (gateway, auth, check-in, notification) need persistent state. A single shared database would be simpler to provision and query across domains (e.g. "friends' streaks" would be one JOIN).

## Decision

Each service owns its own PostgreSQL database (`auth_db`, `checkin_db`, `notification_db`). No service ever reads another's tables; coordination happens through REST calls and RabbitMQ events. Schemas are versioned per service with Flyway.

## Consequences

- Services deploy, migrate, and fail independently — schema changes in one service cannot break another at the database layer.
- Cross-domain reads become explicit integration points: the friends leaderboard composes auth-service data with check-in data over REST (ADR 0004), and the reminder job needed a read model (ADR 0003) instead of a JOIN.
- Locally, three databases run as three containers; in the cloud they are three logical databases on one free-tier Neon instance — the *boundary* is what matters, not the physical hardware.
