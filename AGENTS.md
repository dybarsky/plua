# AGENTS.md — PLUA Cards

Entry point for AI agents (and humans) working in this repo. Read this first, then follow the links.

## What this app is

Offline Android flashcard app for learning vocabulary (Ukrainian ⇄ Polish). A card shows a word; tap to reveal the
translation; swipe for the next card; add / edit / delete word pairs. Words are bundled in an asset file and imported
into SQLite on first launch. No network, no accounts, no permissions.

- Package / namespace: `dmax.plua` (also the Play Store id)
- Language: **Java** (no Kotlin), XML views, single Activity + fragments
- Version: `0.7` / code `8` in the working tree (HEAD is `0.6` / `7`)
- License: MIT

## Documentation map

| Topic | File |
|---|---|
| Architecture, packages, runtime flows | [docs/architecture/ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md) |
| SQLite schema, domain model, `data` asset format | [docs/architecture/DATA_MODEL.md](docs/architecture/DATA_MODEL.md) |
| Verified bugs, WIP state, hazards | [docs/architecture/KNOWN_ISSUES.md](docs/architecture/KNOWN_ISSUES.md) |
| **Upgrade plan (start here for any build/lib work)** | [docs/upgrade/README.md](docs/upgrade/README.md) |
| Gradle / AGP / JDK / `build.gradle` | [docs/upgrade/01-toolchain-gradle-agp.md](docs/upgrade/01-toolchain-gradle-agp.md) |
| Libraries, support → AndroidX, Dagger | [docs/upgrade/02-dependencies-androidx.md](docs/upgrade/02-dependencies-androidx.md) |
| compile/target/min SDK, platform behavior changes | [docs/upgrade/03-sdk-and-platform-behavior.md](docs/upgrade/03-sdk-and-platform-behavior.md) |
| Deprecated APIs, optional modernization | [docs/upgrade/04-code-modernization.md](docs/upgrade/04-code-modernization.md) |
| R8, lint, tests, signing, Play release | [docs/upgrade/05-r8-lint-testing-release.md](docs/upgrade/05-r8-lint-testing-release.md) |

## Repository layout

```
build.gradle              single-module build, Groovy DSL, no settings.gradle yet
proguard-android.txt      project-local R8/ProGuard rules (legacy, see upgrade/05)
gradle/wrapper/           wrapper (8.10.2 in working tree)
src/main/java/dmax/plua/  app code (see ARCHITECTURE.md)
src/main/res/             layouts (a_ activity, f_ fragment, v_ view), values, values-uk (Ukrainian), mipmaps
src/main/assets/          data (word list), about.html, help.html
src/test/java/dmax/plua/  3 JUnit4 unit tests, no instrumentation tests
PRIVACY.md  README.md  LICENSE
```

There is **no `app/` module**: the root project is the app module and sources live in `src/` at the repo root.

## Build / test commands

```bash
# JDK matters. Use JDK 17 or 21 for the Gradle run unless the upgrade is done (see upgrade/01).
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

**Current state (verified 2026-11-17): the project does NOT build as-is on this machine.**
1. Gradle 8.10.2 cannot run on JDK 25 (`Unsupported class file major version 69`); the Android Studio JBR here is also 25.
2. `compileSdk 34` is not installed locally (installed platforms: `android-36`, `android-37.0`).
3. `AGP 8.8.0-beta01` is a pre-release.
4. ~~`assets/data` header uses `POLISH`, not a `Language` value~~ — resolved: the app's language pair is Ukrainian ⇄ Polish
   again (`Language.POLISH`, table `pl`), matching the published 0.6 DB schema (see KNOWN_ISSUES P0-1).

## Conventions in this codebase

- Java 7-style code: no lambdas, anonymous inner classes, 4-space indent, `//~` as section separators.
- File header javadoc: `Created by Maxim Dybarsky | <email> on <date>`. Don't add it to new files; don't strip it from existing ones.
- Resource prefixes: `a_*.xml` activity layouts, `f_*.xml` fragment layouts, `v_*.xml` custom/item views, `ic_*` icons.
- Do **not** `switch` on `R.id.*` / `R.*` values — R fields are non-final (that's why recent commits convert switches to `if` chains).
- Strings: English in `values/strings.xml`, Ukrainian in `values-uk/strings.xml`. Keep both in sync.
- UI state lives in fragments / view holders (`CardViewHolder`); persistence goes only through `DataSource`.
- Words are stored lower-cased by the importer; the unique constraint is per language table.

## Rules for agents

- **Never commit or print the keystore password.** `build.gradle` signs releases with `~/.android/dmax.keystore`; the
  real password belongs in `~/.gradle/gradle.properties` (`keystorePassword`), not in the repo.
- The working tree has substantial **uncommitted migration work** (AGP/Gradle bump, vendored `SpotsDialog`, `switch`→`if`
  rewrites, manifest `exported`). Don't revert it; `git status` first.
- Don't change the DB schema or table names without a real migration — the DB is `version 1` with an empty
  `onUpgrade`, and installed users have data in it (`ua`, `pl`, `link`).
- Run `./gradlew testDebugUnitTest` after touching `domain/`, `DataSource` or `persist/`.
- Prefer fixing items from KNOWN_ISSUES before starting the upgrade — the first two are functional regressions.
