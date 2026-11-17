# Known issues & hazards

Verified by reading the code / running the build on 2026-11-17. Line numbers refer to the working tree.
Severity: **P0** = app broken, **P1** = real bug / blocks upgrade, **P2** = risk or cleanup.

## Functional regressions in the uncommitted tree

### P0-1 — `assets/data` header is `POLISH` → importer crash on first launch — RESOLVED
Resolved by restoring Polish as the app's second language (reversal of commit `44bf59d` "Move to german"):
`Language` is `POLISH, UKRAINIAN` again, table `pl` / `Constants.TABLE_POLISH`, flag `R.mipmap.ic_polish`, string
`polish`, `DependenciesProvider.DEFAULT = Language.POLISH`. The published 0.6 app used table `pl`, so installed
databases keep working with no migration (`DATABASE_VERSION` stays 1). `assets/data` (header `UKRAINIAN|POLISH`, 481
lines) is valid again; one typo was fixed (`Załatwic` → `Załatwię`).
Remaining hazard (P2): `Importer.getLanguages()` (`Importer.java:155`) still calls `Language.valueOf` on the file
header and does not catch `IllegalArgumentException`; a future bad header would crash again. Defensive fix: catch it
and return `null`.

### P0-2 — Save button in `LinkDetailFragment` is inverted
`LinkDetailFragment.java:123`: `if (item.getItemId() != R.id.save) { save(); return true; }`.
The `switch`→`if` rewrite flipped the condition: pressing **Save** falls through to `super` (nothing saved), any
other item triggers a save. Fix: `== R.id.save`.

### P1-3 — Release signing password is now a literal
`build.gradle` signing config: `storePassword "keystorePassword"` / `keyPassword "keystorePassword"` — the string
literal, whereas `HEAD` read the Gradle property `keystorePassword` (defined in `~/.gradle/gradle.properties`).
A release build would fail to sign (wrong password). Restore the property lookup (snippet in
[upgrade/01](../upgrade/01-toolchain-gradle-agp.md#signing)). Never put the real password in the repo.

## Resolved

- **Unit tests did not compile** (`LinkEqualsTest` / `WordEqualsTest` use `Language.POLISH`, which was missing from the
  enum) — resolved together with P0-1; `./gradlew testDebugUnitTest` compiles and runs again.

## Build

### P1-4 — Project does not build in this environment
- Gradle 8.10.2 on JDK 25 → `BUG! exception in phase 'semantic analysis' … Unsupported class file major version 69`.
- `compileSdk 34` platform not installed (only `android-36`, `android-37.0`; build-tools `36.0.0`, `37.0.0`).
- `AGP 8.8.0-beta01` is a pre-release; no `settings.gradle`; no `gradle.properties` in the repo.
Fix path: [upgrade/README.md](../upgrade/README.md).

### P2-5 — Manifest still has `package="dmax.plua"` while `namespace` is set in Gradle
Redundant (warning today, to be removed). No `applicationId` is set in `defaultConfig`; it silently falls back to the
namespace `dmax.plua`. Make it explicit so the Play id can never drift.

### P2-6 — `proguard-android.txt` is stale
Keeps `dmax.dialog.**` (library no longer used; code now lives in `dmax.plua.ui.dialog`), `-dontwarn android.support.**`,
and disables optimization/obfuscation (`-dontoptimize`, `-dontobfuscate`). See [upgrade/05](../upgrade/05-r8-lint-testing-release.md).

## Runtime / code

- **P2 — Uncaught `ActivityNotFoundException`** in `AboutFragment.sendFeedback()` (`:92`) when no mail app is installed.
- **P2 — Thread-safety** of `DataSource.links` and the shared `Dao<Word>` between the UI thread and `Importer`'s
  background thread (see ARCHITECTURE "Threading").
- **P2 — `Importer` leaks / outlives the Activity** (holds Context + dialog; `dialog.dismiss()` on a destroyed window can throw).
- **P2 — Non-atomic `addWords`**: orphan word rows when the second word is a duplicate; no transaction.
- **P2 — `MainActivity.onBackPressed`** (`:66`) inspects the *framework* `getFragmentManager()` back stack, but all
  fragments are *support* fragments, so the branch is dead and `super.onBackPressed()` does the real work. With
  targetSdk 36 (predictive back) `onBackPressed` isn't called at all.
- **P2 — Static mutable `Language.codeName`**: null (→ `toString()`/spinner text null) if anything reads it before
  `MainActivity.initLanguageCodeNames()`.
- **P2 — Selected language is not persisted**; resets to Polish on every launch.
- **P2 — Spaced-repetition feature is dead code**: priority buttons commented out, `PrioritySorter` unused, links are
  shuffled. README still advertises it.
- **P2 — Crossed FKs / misleading column names** in the `link` table: `original` is declared `REFERENCES pl(id)` but
  holds the Ukrainian id; `translation` is declared `REFERENCES ua(id)` but holds the Polish id. Same as in the published
  0.6 schema; harmless because FK enforcement is off, but it blocks any future `PRAGMA foreign_keys=ON` / Room schema
  validation without a migration (see DATA_MODEL).
- **P2 — `Importer` ignores `addWords` result and inserts without a transaction** (481 lines × 3 inserts, auto-commit).
- **P2 — Hardcoded Gmail address** in `strings.xml` (`send_feedback`) — intended, but note it when publishing.

## Docs / policy

- **PRIVACY.md is inaccurate**: it claims the app collects IP address, usage analytics and approximate location and
  shares data with third parties. The app declares no permissions, has no network code and no analytics SDKs
  (verified: manifest has no `<uses-permission>`; dependencies are only support libs + Dagger). Looks like a generic
  template. Owner should rewrite it, and keep the Play Console "Data safety" form consistent.
- README mentions spaced repetition as working; see above. Screenshot link is a dead-prone googleusercontent URL.
