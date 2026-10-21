# Credit Titan AI — Release Safety Gate

This branch is isolated from main. Do not distribute its APKs until upgrade tests pass.

## Confirmed baseline
- applicationId: com.credittitanai.app
- namespace: com.credittitan.ai
- versionCode: 2; versionName: 1.0.1
- minSdk: 24; targetSdk: 35
- Current local data: SharedPreferences titan_local
- Existing application sets allowBackup=false

## Non-negotiable upgrade requirements
1. Preserve applicationId across updates.
2. Sign all upgrades with the same trusted signing certificate. Never store a private signing key in Git.
3. Increment versionCode monotonically; use a controlled release sequence.
4. Compare SHA-256 signing certificate fingerprints of installed and candidate APKs using apksigner verify --print-certs.
5. Test installing candidate APK over an existing version without uninstalling, then confirm saved local data persists.
6. Inspect APK contents and size, launch activity, manifest, resources, and compiled classes. Small size alone is not failure.
7. Test all tabs, FICO Booster Plus guidance/completion/reversal, report notes, dispute drafts, and progress controls.
8. Confirm demo data is labeled and no real credit bureau, FICO, or AI connection is falsely claimed.
9. Block release on failed tests; distribute only verified release artifacts.
10. Keep credentials and consumer financial data out of this public repository.

## Initial audit findings
The Java MainActivity contains real onClick listeners for FICO Booster Plus. Functionality remains unverified on-device. Other features are demonstration-only. Exact APK install failure cause and signing continuity remain unverified.

## Recovery plan
First collect the previously installed APK (if available) and candidate APK, inspect package IDs, version codes and signer fingerprints. If the original signer is lost, document a one-time migration before establishing a persistent release key. Never claim an in-place update is possible across incompatible signatures.

Owner credit: Lanty Kennedy — Creator, Developer & Designer.
