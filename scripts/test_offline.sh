#!/usr/bin/env bash
set -euo pipefail
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT
java -m jdk.compiler/com.sun.tools.javac.Main -d "$OUT" app/src/main/java/com/credittitan/ai/CreditTools.java app/src/main/java/com/credittitan/ai/LocalReports.java tests/java/OfflineToolsTest.java
java -cp "$OUT" OfflineToolsTest
