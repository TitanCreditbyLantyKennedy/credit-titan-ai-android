#!/usr/bin/env bash
set -euo pipefail
mkdir -p .validation
# Prove a missing production identity cannot yield an unsigned/debug release.
if env -u TITAN_KEYSTORE_PATH -u TITAN_STORE_PASSWORD -u TITAN_KEY_ALIAS -u TITAN_KEY_PASSWORD \
    gradle --no-daemon :app:assembleRelease > .validation/missing-signing.log 2>&1; then
  echo 'ERROR: release unexpectedly built without signing credentials'; exit 1
fi
if ! grep -q 'Release signing requires all four TITAN signing environment variables' .validation/missing-signing.log; then
  cat .validation/missing-signing.log; exit 1
fi
# Disposable test identity, never a distribution key. Password is intentionally public.
export TITAN_KEYSTORE_PATH="$PWD/.validation/test.jks"
export TITAN_STORE_PASSWORD=fixture-password TITAN_KEY_PASSWORD=fixture-password TITAN_KEY_ALIAS=fixture
keytool -genkeypair -noprompt -keystore "$TITAN_KEYSTORE_PATH" -storepass:env TITAN_STORE_PASSWORD -keypass:env TITAN_KEY_PASSWORD -alias fixture -dname 'CN=Credit Titan CI Test Only' -keyalg RSA -keysize 2048 -validity 30
CODE=$(sed -n 's/^versionCode=//p' version.properties)
# Build the real prior source, not the candidate with a lower version number.
BASE_REF=59f18ef76d3b604e08729f9b31c4ec36976dd014
mkdir -p .validation/previous-source
git archive "$BASE_REF" | tar -x -C .validation/previous-source
gradle --no-daemon -p .validation/previous-source :app:assembleRelease
cp .validation/previous-source/app/build/outputs/apk/release/app-release.apk .validation/previous.apk
TITAN_VERSION_CODE="$CODE" gradle --no-daemon :app:assembleRelease :app:assembleReleaseAndroidTest :app:lintRelease
cp app/build/outputs/apk/release/app-release.apk .validation/candidate.apk
cp app/build/outputs/apk/androidTest/release/app-release-androidTest.apk .validation/probe.apk
bash scripts/mismatch_fixture.sh
