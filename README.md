# RSS Core Admin

Professional Android control and monitoring client for the Razeen Secure Solution ecosystem.

- Application: RSS Core Admin
- Package: `com.riyaz.rsscoreadmin`
- Repository: `riyazamra1/RSS-Core-Admin`
- Primary RSS Core endpoint: `https://rsscore.cv`

## Verified RSS Core integration

The Android client uses only endpoints verified from the RSS Core backend:

- `GET /health`
- `GET /api/v1/status`
- `GET /api/v1/projects`

The app verifies the RSS Core service identity before reporting the endpoint healthy.

Administrative server routes protected by `API_TOKEN` are not called directly from Android and no backend secrets are embedded in the APK.

Unsupported areas remain explicitly unavailable until RSS Core exposes a dedicated authenticated Admin API. No fake customer, license, RAY, deployment, email, backup, audit, DNS, TLS-expiry, or failover data is generated.

## Build

GitHub Actions builds the debug APK on pushes and pull requests to `main`, and on manual workflow dispatch.

Build command:

`gradle :app:assembleDebug`

Artifact:

`rss-core-admin-debug`

The workflow installs Gradle 8.11.1 directly, so the repository does not depend on a checked-in Gradle wrapper.