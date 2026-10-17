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

## APK update behavior

- On app launch, the client checks the latest published GitHub Release and prompts when a newer semantic version has an APK asset.
- Selecting **Download update** opens the official release APK link. Android still requires the user to approve installing an APK; ordinary apps cannot silently install themselves.
- The app ID must remain `com.riyaz.rsscoreadmin`, versionCode must increase, and every release must use the same private signing key or Android will reject the update.
- The signed release workflow is `.github/workflows/release-android.yml`. It runs on a `vMAJOR.MINOR.PATCH` tag or manual dispatch and publishes an APK plus SHA-256 checksum.
- Before publishing the first signed release, configure these repository Actions secrets: `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`, and `ANDROID_STORE_PASSWORD`. Keep a secure offline backup of the keystore. Never commit the keystore or its passwords.
- If a previously installed APK was signed with a different key, Android will require that app to be uninstalled before installing the first release-signed APK. After that one-time migration, same-key releases can update in place and preserve app data.
- A new release is not considered live until the release workflow succeeds and the APK asset is visible on the GitHub Releases page.
