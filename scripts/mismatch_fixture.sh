#!/usr/bin/env bash
set -euo pipefail
# Re-sign the candidate with an unrelated disposable key to test Android rejection.
export WRONG_PASSWORD=fixture-password
keytool -genkeypair -noprompt -keystore .validation/wrong.jks -storepass:env WRONG_PASSWORD -keypass:env WRONG_PASSWORD -alias wrong -dname 'CN=Wrong CI Test Only' -keyalg RSA -keysize 2048 -validity 30
"$ANDROID_HOME/build-tools/35.0.0/apksigner" sign --ks .validation/wrong.jks --ks-key-alias wrong --ks-pass env:WRONG_PASSWORD --key-pass env:WRONG_PASSWORD --out .validation/mismatch.apk .validation/candidate.apk
