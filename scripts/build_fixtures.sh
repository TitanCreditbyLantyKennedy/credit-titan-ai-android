#!/usr/bin/env bash
set -euo pipefail
mkdir -p .validation
# Disposable test identity, never a distribution key. Password is intentionally public.
export TITAN_KEYSTORE_PATH="$PWD/.validation/test.jks"
export TITAN_STORE_PASSWORD=fixture-password TITAN_KEY_PASSWORD=fixture-password TITAN_KEY_ALIAS=fixture
keytool -genkeypair -noprompt -keystore "$TITAN_KEYSTORE_PATH" -storepass:env TITAN_STORE_PASSWORD -keypass:env TITAN_KEY_PASSWORD -alias fixture -dname 'CN=Credit Titan CI Test Only' -keyalg RSA -keysize 2048 -validity 30
TITAN_VERSION_CODE=2 gradle --no-daemon :app:assembleRelease
cp app/build/outputs/apk/release/app-release.apk .validation/previous.apk
TITAN_VERSION_CODE=3 gradle --no-daemon :app:assembleRelease :app:assembleReleaseAndroidTest
cp app/build/outputs/apk/release/app-release.apk .validation/candidate.apk
cp app/build/outputs/apk/androidTest/release/app-release-androidTest.apk .validation/probe.apk
bash scripts/mismatch_fixture.sh
