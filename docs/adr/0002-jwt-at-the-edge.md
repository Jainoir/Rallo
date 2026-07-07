# ADR 0002 — JWT validated at the edge, identity forwarded as headers

**Status:** accepted

## Context

Every request needs authentication. Each service could validate the JWT itself (shared secret or JWKS), or validation could happen once at the gateway.

## Decision

The API gateway validates the JWT signature and expiry, then forwards the caller's identity to downstream services as `X-User-Id` / `X-User-Name` / `X-User-Roles` headers. Services trust these headers and never parse tokens.

Because the hosting platform (Render free tier) gives backend services public URLs, header trust alone would be forgeable. The gateway therefore stamps every forwarded request with a shared secret (`X-Gateway-Secret`); each service rejects requests without it (constant-time comparison), keeping only Swagger and health endpoints public. The mechanism is disabled when the secret is unconfigured, so local development needs no setup. Service-to-service calls (ADR 0004) reuse the same secret.

## Consequences

- Token parsing logic and the JWT dependency live in two places (gateway, auth service) instead of four.
- Services stay small: business code reads a header, not a security context.
- The shared secret is platform-generated and never handled by humans.
- Trade-off: services cannot do fine-grained, token-based authorization (e.g. scopes) without re-introducing JWT parsing — acceptable at this scale, revisit if roles become richer.
