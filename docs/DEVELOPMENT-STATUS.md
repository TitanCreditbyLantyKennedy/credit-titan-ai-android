# Credit Titan AI — development status

Owner / Creator, Developer & Designer: **Lanty Kennedy**.

## Current implementation

Version 1.0.3 / code 4 is an **offline Android foundation**, not a public production service. It preserves `com.credittitanai.app`, the shield, and the approved ice-blue/silver/navy palette. Five fixed tabs expose Home, Reports, Disputes, Coach, and More. More contains the action plan, bureaus, progress, card examples, Guardian, and privacy controls.

| Area | Working locally | Still needed for live operation |
| --- | --- | --- |
| Dashboard | Clearly fictional score gauge, saved-plan progress, workspace counts | Consented score/report provider and model/version/date attribution |
| Reports | Private PDF copying, type-header/size/count checks, bureau assignment, deletion, notes | PDF extraction and OCR, supported-format fixtures, cross-bureau normalized records, evidence citations, consented AI review |
| Disputes | Bureau selection, editable note-based draft, overwrite confirmation, reviewed text export | Multiple dispute records, evidence attachment, current verified addresses, user-approved mailing, delivery/response history |
| Coach | Prepared educational topics and input-validated utilization calculator | Authenticated AI backend, grounded responses and refusal/escalation tests |
| FICO Booster Plus | Six guidance dialogs, persistent completion, progress | No score prediction is implemented; future data must identify the scoring model |
| Bureaus / Guardian | Explicitly disconnected status; local review checklist | Bureau agreements, monitoring consent, event ingestion and real alert delivery |
| Marketplace | Fictional example terms and offer-detail dialogs | Bank agreements, current disclosures, compensation labels, privacy-preserving matching and approved affiliate tracking |
| Privacy | No internet permission, private storage, screenshot protection, backup disabled, confirmed local deletion | Threat model, authentication/app lock, backend encryption/key policy, deletion/retention/export controls, security review |

The planned marketplace business model remains bank-paid advertising plus approved-account commissions, with consumers not charged for offer comparison. Paid placement and compensation must be disclosed when real products are available.

## Compatibility

`report_notes`, `dispute_draft`, `boost0..5`, and `goal0..3` retain their names and preference file `titan_local`. New report copies live under private `files/reports/`, with an assigned bureau stored under `bureau_<internal filename>`. No full original document filename is recorded. Android backup stays disabled. Uninstalling will remove this private workspace; use the signing continuity gates rather than telling users to uninstall for routine updates.

A new recipient selection applies to the next generated draft, not an existing edited draft. There is one saved draft in this foundation. Generating over it requires confirmation; exporting retains a copy outside app-private storage. Draft generation includes the user's notes verbatim and does not analyze them or establish a legal violation. No letter, application, or payment is sent.

PDF import uses the Android document picker, copies off the UI thread, limits nine files to 20 MB each, requires a PDF header, and removes partial copies on handled failures. It does not validate full PDF structure or render/parse a report. Local deletion includes interrupted `.pending` files. Export is a user-selected plain-text file; exports and original source documents are not removed by clearing local workspace data.

## Verification

- `bash scripts/test_offline.sh`: executable Java tests of utilization, draft recipients/empty inputs, private PDF copying, invalid/truncated/oversized/interrupted imports, count limits, traversal rejection, and deletion of interrupted copies.
- `python3 -m unittest discover -s tests -v`: APK identity, certificate, version, debug/release, and archive policy tests.
- `python3 scripts/check_version.py --base-ref origin/fix/apk-update-reliability`: source version discipline.
- `bash scripts/build_fixtures.sh`: temporary test keys only; builds actual prior source at PR #5 commit `59f18ef...` and the current candidate.
- `python3 scripts/test_upgrade.py ...`: refuses physical devices and pre-existing installations; emulator tests launch, same-key update, note/draft/checklist/PDF preservation, downgrade and wrong-key rejection, then real screen interactions.
- Test-only instrumentation captures fictional-data UI screenshots by temporarily clearing screenshot protection in the test process. Production continues to use `FLAG_SECURE`.

The fixture key must never be distributed. Screenshots alone cannot establish correct live data handling, accessibility across all devices, or production readiness. Run the canonical API 24/35 emulator checks and review screenshots at normal and larger font sizes before merging. The imported report fixture is synthetic and contains no consumer data.

## Local validation record — October 21, 2026

A clean build passed `:app:assembleDebug`, `:app:assembleRelease`, `:app:assembleReleaseAndroidTest`, `:app:lintDebug`, and `:app:lintRelease` using Java 17, Gradle 8.9, platform 35 and build-tools 35.0.0. Debug and release lint XML reports contain zero issues. Release packaging used a disposable local test key, not a production identity. The APK verifier confirms package `com.credittitanai.app`, versionCode 4, the expected launcher, valid signature/archive and a non-debuggable release.

The offline Java suite passes 43 assertions; all nine Python APK-policy tests pass. Shell syntax, Python compilation, workflow YAML parsing and source version checks pass.

**Not yet run for this branch:** API 24/35 emulator UI tests, actual runtime upgrade tests, screenshot review, physical-device checks and real-key installed-device continuity. These remain required before merge/distribution. API 35 CI is configured for 1.3x font size. This workspace has no accelerated Android emulator. Automatic approval review blocked the branch push; no new remote PR, merge or release was created by this work.

## Next implementation order

1. Pass full Android build, lint, emulator interaction and genuine prior-source upgrade tests; fix all failures and visually inspect small-screen and larger-font layouts.
2. Resolve installed-app signing identity and owner-controlled release key recovery/creation/backup. Run the real-key baseline upgrade gate in `APK-UPDATES.md`. Never commit keys, passwords, provider credentials, or consumer reports.
3. Add normalized report/account/evidence records and a reproducible parser pipeline with synthetic/redacted fixtures for supported formats. Record extraction uncertainty; never silently treat unreadable PDFs as clean reports.
4. Add a secure authenticated backend for consented processing. Keep service credentials off Android. Define access control, rate limits, deletion/retention, redacted operational logs, provider failures, and incident recovery before connecting real consumers.
5. Implement evidence-linked potential-inaccuracy review across Experian, Equifax and TransUnion. Users approve selected items and each bureau letter. Add structured multi-case status/history, verified delivery events, response tracking and reviewed deadline rules.
6. Connect approved scoring/monitoring providers with explicit consent and data provenance. Add bank-paid marketplace partnerships and transparent paid placements. Validate real disclosure, error and cancellation flows.
7. Complete privacy/legal/product reviews, accessibility and device testing, closed beta and staged release. Expand iPhone after the shared service contracts are stable.

## Owner input needed

Development can continue without production secrets. Distribution needs the actual installed APK/certificate and recoverable matching signing key, or an explicit migration decision if that key is lost; an owner-controlled permanent release keystore and secured backup; release environment configuration; and an approved previous APK baseline. Do not send secret passwords in chat. Live functionality later requires chosen providers/contracts, backend accounts, and credentials configured through their secure secret stores.

Nothing in this work authorizes merging or distributing unvalidated APKs. PR #5 remains the signing prerequisite.
