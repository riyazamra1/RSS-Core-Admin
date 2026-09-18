# RSS Core Admin — Verified API Contract

Inspected: 2026-09-19

## Verified backend

Repository: `riyazamra1/RSS-Core`

Production endpoint: `https://rsscore.cv`

## Public endpoints verified

### Health

`GET /health`

Used by RSS Core Admin to verify connectivity and RSS Core service identity.

### Status

`GET /api/v1/status`

Used for available RSS Core status/version/runtime information.

### Projects

`GET /api/v1/projects`

Used for the project registry.

## Administrative API gap

The inspected backend does not expose a verified Android-admin API for administrator authentication, customers, licenses, RAY Mission Control, deployments, email telemetry, backups, centralized errors, audit/security events, DNS/domain expiry, TLS expiry, endpoint discovery, or infrastructure integrations.

Privileged server-side routes use `API_TOKEN`; the Android client does not contain or call that token.

## Endpoint resilience

Current verified configuration:

- Primary: `https://rsscore.cv`
- Secondary: not configured
- Discovery: not exposed
- Failover: not claimed

No unsupported endpoint or fake redundancy is introduced.