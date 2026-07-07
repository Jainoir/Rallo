# ADR 0003 — Event-built read model for streak reminders

**Status:** accepted

## Context

The nightly reminder job must find streaks that are at risk (last check-in yesterday) or broken. That data lives in the check-in service. Two options:

1. The notification service calls the check-in service's API every night.
2. The notification service builds its own read model by consuming the `checkin.recorded` events it already receives.

## Decision

Option 2. Every consumed check-in event upserts a `goal_activity` row (goal, owner, frequency, last check-in date, current streak). The nightly job (20:00 UTC) queries only this local table: daily goals last checked in yesterday get a `STREAK_REMINDER` (once per day), older ones with a positive streak get a `STREAK_BROKEN` notification and are zeroed.

## Consequences

- The reminder job has no runtime dependency on the check-in service — it works even when that service is down or asleep (relevant on free-tier hosting).
- The read model is eventually consistent: it only knows what events said. If events were lost, reminders could be wrong — acceptable for nudges, unacceptable for money; the pattern is chosen per use case.
- Scheduling runs in UTC while streak *calculation* uses the user's browser-sent timezone (`X-Timezone`). A reminder may arrive at an odd local hour for far-eastern timezones; per-user scheduling is future work and would only touch the notification service.
