# Deploying Rallo to the cloud

Target: **Fly.io** (containers, Montreal region `yul`) + **Neon** (managed Postgres) + **CloudAMQP** (managed RabbitMQ). All config files are already in the repo; this guide is the ~20 minutes of account setup only a human can do. After step 6, every merge to `main` deploys automatically.

## 1. Accounts (free tiers)

- [Fly.io](https://fly.io) — requires a credit card for identity; small monthly cost (see notes below)
- [Neon](https://neon.tech) — free tier, no card
- [CloudAMQP](https://www.cloudamqp.com) — "Little Lemur" free plan

## 2. Install flyctl and create the apps

```bash
# Windows (PowerShell)
pwsh -Command "iwr https://fly.io/install.ps1 -useb | iex"

fly auth login

fly apps create rallo-jainoir-auth
fly apps create rallo-jainoir-checkin
fly apps create rallo-jainoir-notify
fly apps create rallo-jainoir-gateway
fly apps create rallo-jainoir-web
```

If a name is taken, pick another and update it in the matching `fly.*.toml` **and** in the `*.internal` URLs in `fly.api-gateway.toml` and `frontend/fly.toml`.

## 3. Databases (Neon)

Create one Neon project with **three databases**: `auth_db`, `checkin_db`, `notification_db` (one instance, three logical databases — fine for a demo; the code never crosses them). For each service, set its connection secrets (values from Neon's connection details, use the **pooled** connection string):

```bash
fly secrets set -a rallo-jainoir-auth \
  SPRING_DATASOURCE_URL="jdbc:postgresql://<neon-host>/auth_db?sslmode=require" \
  SPRING_DATASOURCE_USERNAME="<user>" \
  SPRING_DATASOURCE_PASSWORD="<password>"

fly secrets set -a rallo-jainoir-checkin \
  SPRING_DATASOURCE_URL="jdbc:postgresql://<neon-host>/checkin_db?sslmode=require" \
  SPRING_DATASOURCE_USERNAME="<user>" \
  SPRING_DATASOURCE_PASSWORD="<password>"

fly secrets set -a rallo-jainoir-notify \
  SPRING_DATASOURCE_URL="jdbc:postgresql://<neon-host>/notification_db?sslmode=require" \
  SPRING_DATASOURCE_USERNAME="<user>" \
  SPRING_DATASOURCE_PASSWORD="<password>"
```

## 4. RabbitMQ (CloudAMQP)

Create a Little Lemur instance, copy its **AMQPS URL**, then:

```bash
fly secrets set -a rallo-jainoir-checkin SPRING_RABBITMQ_ADDRESSES="amqps://<user>:<pass>@<host>/<vhost>"
fly secrets set -a rallo-jainoir-notify  SPRING_RABBITMQ_ADDRESSES="amqps://<user>:<pass>@<host>/<vhost>"
```

(`spring.rabbitmq.addresses` overrides the host/port in application.yml — no code change needed.)

## 5. JWT secret

Generate one strong secret and give it to the three apps that sign or verify tokens:

```bash
# any 48+ char random string; PowerShell:
#   -join ((48..57)+(65..90)+(97..122) | Get-Random -Count 48 | % {[char]$_})
fly secrets set -a rallo-jainoir-auth    JWT_SECRET="<the-secret>"
fly secrets set -a rallo-jainoir-gateway JWT_SECRET="<the-secret>"
fly secrets set -a rallo-jainoir-checkin JWT_SECRET="<the-secret>"
```

## 6. Wire CI

```bash
fly tokens create deploy   # copy the output
```

GitHub → repo → Settings → Secrets and variables → Actions → **New repository secret** → name `FLY_API_TOKEN`, paste the token. Done — the next push to `main` deploys everything (the deploy job skips itself while this secret is missing).

To deploy immediately without waiting for CI, from the repo root:

```bash
fly deploy --remote-only --config fly.auth-service.toml
fly deploy --remote-only --config fly.checkin-service.toml
fly deploy --remote-only --config fly.notification-service.toml
fly deploy --remote-only --config fly.api-gateway.toml
fly deploy frontend --remote-only
```

## 7. Verify

```bash
curl -s -X POST https://rallo-jainoir-gateway.fly.dev/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","email":"demo@example.com","password":"password123"}'
```

Then open **https://rallo-jainoir-web.fly.dev**, register, create a goal, check in. Put that URL in the README.

## Notes

- **Cost**: the two public apps auto-stop when idle (free-ish). The three private Java services don't have public traffic to wake them, so they stay running — roughly $3–7/month total at 512 MB shared-CPU. `fly scale count 0 -a <app>` pauses everything between demo periods.
- **Redis** is not deployed: the gateway only needs it for rate limiting, which isn't enabled yet. Add Upstash later alongside the leaderboard feature.
- **First-boot order**: services tolerate any start order; Hibernate creates schemas on first connect (`ddl-auto: update` — Flyway is on the roadmap).
