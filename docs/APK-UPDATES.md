# Credit Titan AI APK updates

Issue #4 implementation. Package stays `com.credittitanai.app`; namespace and launcher stay `com.credittitan.ai.MainActivity`. The production Java source, shield icon, Lanty Kennedy attribution, and ice-blue/silver/navy UI are unchanged.

## What caused the risk

Both previous workflows produced debug-signed APKs on fresh runners with versionCode 2. A successful build or small APK size does not prove upgrade compatibility. Android requires a compatible signing identity and rejects lower version codes. This change never falls back to debug signing and never asks a user to uninstall.

## Certificate continuity is a prerequisite

Before changing anything on an existing phone, connect it to an authorized Android SDK workstation and inspect the installed base APK:

```bash
adb shell pm path com.credittitanai.app
# Copy the exact base.apk path from that command, then:
adb pull '<exact installed base.apk path>' installed-base.apk
$ANDROID_HOME/build-tools/35.0.0/apksigner verify --print-certs installed-base.apk
$ANDROID_HOME/build-tools/35.0.0/aapt dump badging installed-base.apk
```

Record package, versionCode, certificate SHA-256 and APK SHA-256. Do not infer these from a downloaded file's name or a successful old CI run. Do not clear app data or uninstall to get a green test.

A new dedicated release key cannot silently update an app signed with an unrelated historical debug key. A certificate fingerprint alone cannot recover its private key. If the installed certificate differs, stop rollout and investigate recovery of the original private key and a separately reviewed migration strategy. There is no automatic signing rotation or migration in this patch. Existing local notes have no export flow, so uninstalling may destroy them.

## Owner-controlled release identity

Create the dedicated release key once on a trusted workstation, outside the repository. `keytool` prompts for the password; never paste a password into a command or commit it:

```bash
keytool -genkeypair -v -keystore /secure/offline/credit-titan-release.jks \
  -alias credit-titan-release -keyalg RSA -keysize 4096 -validity 10000
keytool -exportcert -keystore /secure/offline/credit-titan-release.jks \
  -alias credit-titan-release -file /secure/offline/credit-titan-release.der
keytool -printcert -file /secure/offline/credit-titan-release.der
```

Keep encrypted backups of the key and credentials in two owner-controlled locations; verify a restore and confirm the same certificate before enabling CI. Never regenerate the key on a runner. Do not use a debug key for public releases. Creating a new key does not resolve the legacy certificate prerequisite above.

Configure GitHub environment `release-signing` for the main branch only, with required reviewers where supported. Add these **environment secrets**, using GitHub's secure settings or an authenticated administrator workstation (never a PR, log, or chat):

| Secret | Content |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | Base64 of the backed-up dedicated keystore, without line breaks |
| `RELEASE_STORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Existing alias |
| `RELEASE_KEY_PASSWORD` | Alias password |

Set environment variables:

| Variable | Content |
| --- | --- |
| `RELEASE_CERT_SHA256` | Independently verified certificate SHA-256, 64 hexadecimal characters (colons accepted) |
| `PREVIOUS_APK_URL` | HTTPS download URL returning the actual last distributed APK bytes, not an HTML or ZIP page |
| `PREVIOUS_APK_SHA256` | Independently recorded SHA-256 of that APK |

The baseline is mandatory; no network response can replace its pinned hash or certificate. Debug or unsigned baselines are rejected. Never manufacture a baseline to make legacy migration appear validated. Initial establishment of a new release channel requires a separate reviewed bootstrap decision; this workflow intentionally has no first-release bypass. Current secrets and installed-device identity have not been verified by this patch.

## One canonical workflow

`.github/workflows/main.yml` replaces both debug distribution workflows. PRs and pushes run policy tests and disposable-key release upgrade tests on API 24 and API 35; those fixture APKs and keys are never uploaded. They demonstrate Android's behavior, not compatibility with an existing user's key.

Manual `validate_release=true` is restricted to main and the `release-signing` environment. It downloads the pinned previous release, builds with the environment key, checks archive integrity, signatures, package, exact launcher, non-debuggable status, source version, and certificate continuity. It then tests the actual previous APK → candidate on a fresh emulator, with notes, draft and checklist preferences seeded through a same-key instrumentation APK. Wrong-key and downgrade attempts must fail with Android's specific error codes, preserve version/data, and leave the app launchable. The instrumentation probe is test-only and never uploaded.

Only after all these checks pass does the workflow retain the candidate APK and its verification JSON as a review artifact. Nothing creates a GitHub release, publishes to a store, or merges code. Release key files are created mode 0600 in runner temporary storage, Gradle caching is disabled, and cleanup runs on failure. GitHub-hosted runners are ephemeral; never upload the workspace or test fixtures.

## Version discipline

`version.properties` is the source of the next version, initially code 3/name 1.0.2. Increment the code for every distributed binary; PR checks require an increase for app changes. The release gate additionally requires a code strictly greater than the approved previous APK. Range is 1..2100000000. Do not derive codes from workflow run numbers or timestamps. `TITAN_VERSION_CODE` is only used for disposable test fixtures; the real release verifies the APK code against the source file.

After an approved distribution, archive that exact APK and verification JSON durably and update the approved baseline URL/hash before validating the next release. Serializing release validation avoids concurrent jobs, but distribution remains a separate human-controlled step: never distribute two different binaries under the same code, including reruns. Require the canonical tests as branch checks and review changes to signing settings, certificate pin and workflow.

## Validation and merge gate

Local commands with Android SDK 35 and Gradle 8.9:

```bash
python3 -m unittest discover -s tests -v
bash scripts/build_fixtures.sh
# Start a fresh disposable emulator, then:
python3 scripts/test_upgrade.py --previous .validation/previous.apk \
  --candidate .validation/candidate.apk --mismatch .validation/mismatch.apk \
  --probe .validation/probe.apk
```

The test script refuses an already-installed app and never uninstalls or clears data. Do not run test fixtures against a personal phone.

Before merge or distribution, require green canonical CI, restored-key/certificate verification, an approved real baseline, actual signed upgrade tests, and confirmation of the installed device's identity. PR tests alone cannot establish that a lost historical signing key is recoverable. An emulator proves startup and preference preservation, not full production readiness or all device/OEM behavior.

Android references: [App signing](https://developer.android.com/studio/publish/app-signing), [versioning](https://developer.android.com/studio/publish/versioning), [apksigner](https://developer.android.com/tools/apksigner).
