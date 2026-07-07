# ADR 0004 — Leaderboards: composition in the check-in service, Redis as best-effort cache

**Status:** accepted

## Context

Friends/group leaderboards need data from two owners: the auth service knows who your friends and group mates are; the check-in service knows their streaks. Someone has to join them. Options: the frontend makes two calls and joins in the browser; a dedicated aggregator service; or one of the two services composes.

## Decision

The check-in service composes. It calls the auth service (`/api/friends`, `/api/groups/{id}/members`) — authenticated with the same gateway shared secret plus the caller's forwarded identity, so the auth service still enforces membership rules — then computes each participant's best current streak locally and ranks.

Group leaderboards are cached in Redis (60 s TTL, key includes the group and the caller's local date). The cache is **best-effort**: any Redis failure is caught and the leaderboard is computed directly. Redis is therefore optional infrastructure — not deployed in the current cloud setup — and turning it on later is a config change, not a code change.

## Consequences

- One round trip for the client; membership authorization stays with the data owner (auth).
- The check-in service gains a synchronous dependency on the auth service for leaderboards only — core check-in flows still work if auth is down.
- Best-effort caching means a Redis outage degrades to slower responses, never errors — the right trade for a leaderboard (stale or slow is fine; wrong or missing is not).
