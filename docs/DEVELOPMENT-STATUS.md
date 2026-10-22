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
- Test-only instrumentation captures fictional-data UI screenshots by temporarily clearing screenshot protection in the test process. When the older screenshot compositor returns no image, the test renders the actual laid-out native decor. Production continues to use `FLAG_SECURE`.

The fixture key must never be distributed. Screenshots alone cannot establish correct live data handling, accessibility across all devices, or production readiness. Run the canonical API 24/35 emulator checks and review screenshots at normal and larger font sizes before merging. The imported report fixture is synthetic and contains no consumer data. Editor restoration is deferred until the new window attaches, and an old activity explicitly closes its editor dialog during destruction; tests verify the actual restored text and invoke the real Save button.

## Local validation record — October 21, 2026

A clean build passed `:app:assembleDebug`, `:app:assembleRelease`, `:app:assembleReleaseAndroidTest`, `:app:lintDebug`, and `:app:lintRelease` using Java 17, Gradle 8.9, platform 35 and build-tools 35.0.0. Debug and release lint XML reports contain zero issues. Release packaging used a disposable local test key, not a production identity. The APK verifier confirms package `com.credittitanai.app`, versionCode 4, the expected launcher, valid signature/archive and a non-debuggable release.

The offline Java suite passes 43 assertions; all nine Python APK-policy tests pass. Shell syntax, Python compilation, workflow YAML parsing and source version checks pass.

The owner approved source publication on October 22; development/app-foundation is now available in draft PR #6. This local workspace has no accelerated emulator; runtime checks ran on GitHub. Nothing has been merged or released.

## CI validation record — October 22, 2026

[Canonical CI run 39](https://github.com/TitanCreditbyLantyKennedy/credit-titan-ai-android/actions/runs/39746943989) passed both API 24 (Android 7, normal text) and API 35 (Android 15, 1.3x text) on app/test source commit `de69bdd376f4da2c87502b986bab836f27b4e118`.

Both jobs pass the 43 offline assertions, nine APK policy tests, source-version checks, missing-signing-credential rejection, actual previous-source and current release/test builds, release lint, package/launcher/signature/version/archive checks, fresh install and launch, in-place upgrade with saved notes/draft/checklist/PDF preservation, downgrade rejection, unrelated-key rejection, screen navigation, note editing, bureau-specific draft generation, unsaved editor recreation and Save, utilization calculation, checklist persistence, and privacy cancellation. Both jobs retain fictional-data UI screenshots for review.

The tests require a genuinely new attached foreground activity after recreation, check the original unsaved text, and invoke the real Save button. Production editor dialogs close with their old activity and restore after the new decor attaches. Navigation labels and score spacing were adjusted after compact-screen/larger-text screenshot review. Navy window and system-bar colors are reapplied on resume/focus after dialogs and activity recreation. The current PR head runs the same canonical workflow for subsequent refinements.

**Remaining gates:** physical-device testing, full accessibility/device coverage, recovery or establishment of the owner-controlled permanent release identity, installed-phone certificate verification, an approved real release baseline or separately reviewed bootstrap/migration decision, and actual real-key upgrade verification. Disposable CI identities do not establish compatibility with an installed consumer copy. Live-service integrations remain incomplete as listed above.

## Next implementation order

1. Complete manual document-picker/export round trips, physical-device compatibility checks and full accessibility coverage. Automated builds, lint, emulator interactions, genuine prior-source upgrades and compact/larger-text screenshot review are recorded above.
2. Resolve installed-app signing identity and owner-controlled release key recovery/creation/backup. Run the real-key baseline upgrade gate in `APK-UPDATES.md`. Never commit keys, passwords, provider credentials, or consumer reports.
3. Add normalized report/account/evidence records and a reproducible parser pipeline with synthetic/redacted fixtures for supported formats. Record extraction uncertainty; never silently treat unreadable PDFs as clean reports.
4. Add a secure authenticated backend for consented processing. Keep service credentials off Android. Define access control, rate limits, deletion/retention, redacted operational logs, provider failures, and incident recovery before connecting real consumers.
5. Implement evidence-linked potential-inaccuracy review across Experian, Equifax and TransUnion. Users approve selected items and each bureau letter. Add structured multi-case status/history, verified delivery events, response tracking and reviewed deadline rules.
6. Connect approved scoring/monitoring providers with explicit consent and data provenance. Add bank-paid marketplace partnerships and transparent paid placements. Validate real disclosure, error and cancellation flows.
7. Complete privacy/legal/product reviews, accessibility and device testing, closed beta and staged release. Expand iPhone after the shared service contracts are stable.

## Owner input needed

Development can continue without production secrets. Distribution needs the actual installed APK/certificate and recoverable matching signing key, or an explicit migration decision if that key is lost; an owner-controlled permanent release keystore and secured backup; release environment configuration; and an approved previous APK baseline. Do not send secret passwords in chat. Live functionality later requires chosen providers/contracts, backend accounts, and credentials configured through their secure secret stores.

Nothing in this work authorizes merging or distributing unvalidated APKs. PR #5 remains the signing prerequisite.
