# Credit Titan AI Android

Creator, Developer & Designer: **Lanty Kennedy**.

Native Android offline foundation with ice-blue, silver and navy styling, shield identity, local report workspace, user-reviewed dispute drafting, educational coaching and action-plan progress. Package: `com.credittitanai.app`.

The app is clearly labeled as a demo: no live AI, bureau data, monitoring, card offers or dispute submissions are connected. Private PDF imports are stored, not parsed or analyzed.

- [Implemented features, verification and remaining work](docs/DEVELOPMENT-STATUS.md)
- [APK signing, updates and distribution gates](docs/APK-UPDATES.md)

Android builds use only `app/src/main/` and the `:app` Gradle module. Legacy duplicate root source/manifest templates have been removed to prevent editing unbuilt files. No CI debug APK is distributed. Temporary-key APKs are for disposable emulator tests only.

Local checks: `bash scripts/test_offline.sh` and `python3 -m unittest discover -s tests -v`. Android compile/build needs Java 17, Gradle 8.9, Android platform 35 and build-tools 35.0.0. Full emulator tests run through the canonical workflow; release signing requires owner-controlled secrets, never a random debug key.
