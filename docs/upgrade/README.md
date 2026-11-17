# Upgrade plan — overview

Goal: bring PLUA Cards from a 2018-era stack (support libraries, SDK 28 → half-migrated to 34) to a current,
Play-compliant stack. Analysis date: **2026-11-17**.

> **Status (branch `upgrade/agp9`):** steps 0–3 and the R8 part of step 5 are applied and committed — Gradle 9.6.1,
> AGP 9.5.1, SDK 36, minSdk 23, AndroidX/Material, Dagger 2.60.1, `proguard-rules.pro` + `shrinkResources`.
> `./gradlew help assembleDebug testDebugUnitTest lintDebug bundleRelease` is green on JDK 25. The "working tree"
> table below describes the pre-upgrade state and is kept for history. Still open: step 4 leftovers, lint baseline,
> instrumented tests, PRIVACY.md/README, on-device verification.

## Where the project is today

The working tree is a **partial migration** from the committed `HEAD` (AGP 3.1.4 / Gradle 4.6 / SDK 28).

| Item | `HEAD` (committed) | Working tree (uncommitted) | Notes |
|---|---|---|---|
| Gradle wrapper | 4.6 (`-all`) | 8.10.2 (`-bin`) | wrapper jar + `gradlew` regenerated, `gradlew.bat` untracked |
| AGP | 3.1.4 | **8.8.0-beta01** | pre-release; old DSL still in use |
| Repos | `jcenter()` (dead) | `mavenCentral()` | ✓ |
| Dependency configs | `compile`/`provided` | `implementation`/`annotationProcessor` | ✓ |
| `namespace` | – | `dmax.plua` | manifest `package=` still present |
| compileSdk / targetSdk | 28 / 28 | 34 / 34 | 34 not installed locally |
| minSdk | 15 | 15 | |
| Support libs | 28.0.0 | 28.0.0 | **not migrated to AndroidX** |
| Dagger | 2.15 | 2.15 | |
| JUnit | 4.12 | 4.12 | |
| spots-dialog lib | `com.github.d-max:spots-dialog:1.1` | vendored into `ui/dialog` | ✓ (JCenter gone) |
| Manifest | – | `android:exported="true"` on launcher activity | ✓ |
| R usage in `switch` | `switch` | `if` chains | ✓ (non-final R) — introduced bug P0-2 |

## Target (as of 2026-11-17)

| Item | Target | Source / confidence |
|---|---|---|
| **AGP** | **9.5.1** (latest stable) | [AGP release notes](https://developer.android.com/build/releases/gradle-plugin) — max API 37, needs Gradle ≥ 9.6.0, JDK ≥ 17, Build Tools ≥ 36.0.0 |
| **Gradle** | **9.6.1** (min for AGP 9.5; already cached on this machine) | newest Gradle is 9.9.1 ([gradle.org/releases](https://gradle.org/releases/)); 9.6.1 is the safe documented floor |
| **JDK for Gradle** | **21** (17 is the minimum) | JDK 25 should work with Gradle ≥ 9.1 / AGP 9.5 (AGP fixed a JDK 25 warning in 9.5.0-alpha08) but was **not tested here** |
| **compileSdk** | **36** (37 optional; AGP 9.5 supports up to 37) | both platforms installed |
| **targetSdk** | **36** | Play: new apps & updates must target 36 since 2026-08-31; existing apps ≥ 35 to stay visible to new users on new devices — *as reported by search results; confirm on the [Play requirements page](https://developer.android.com/google/play/requirements/target-sdk)* |
| **minSdk** | **23** recommended (21 absolute floor for AndroidX appcompat/material) | product decision — see 03 |
| **Build tools** | 36.0.0 (installed) | AGP min |
| **AndroidX** | appcompat 1.7.x, material 1.13.x, cardview 1.0.0, fragment 1.8.x, viewpager (1.1.0) or viewpager2 1.1.0 | **verify latest on Google Maven when you do it**; versions listed in 02 are the last ones known to me |
| **Dagger** | **2.60** (≥ 2.59.2) | [dagger.dev](https://dagger.dev/) |
| **JUnit** | 4.13.2 | |
| **Java source level** | 17 (`compileOptions`) | AGP 9 default is 11 |

## Recommended order

Do these as separate commits so each can be built/bisected.

0. **Stabilize** (no tooling change) — fix P0-1, P0-2, P1-3 from [KNOWN_ISSUES](../architecture/KNOWN_ISSUES.md);
   commit the current WIP (wrapper, vendored dialog, `gradlew.bat`). Add a minimal unit-test safety net (05).
1. **Toolchain** — [01](01-toolchain-gradle-agp.md): JDK 21, `settings.gradle`, Gradle 9.6.1, AGP 9.5.1, new DSL names,
   `gradle.properties`, signing from properties, SDK 36. Leave libraries on support libs *only if* the build allows
   (it won't with AGP 9's `useAndroidX=true` default → go straight to step 2 in the same branch; they're one logical change).
2. **AndroidX + libraries** — [02](02-dependencies-androidx.md): support → AndroidX/Material via Android Studio's
   *Refactor ▸ Migrate to AndroidX*, Dagger 2.60, JUnit 4.13.2, drop `jsr250-api`.
3. **minSdk + SDK behavior** — [03](03-sdk-and-platform-behavior.md): raise minSdk, delete API-21 branches/`values-v21`,
   edge-to-edge, predictive back, large-screen rules, manifest cleanup. Verify on API 23, 34, 36 emulators.
4. **Deprecated API cleanup** — [04](04-code-modernization.md): `AsyncTask`, `getFragmentManager`, `onActivityCreated`,
   options-menu API, `getSerializable`, etc. Pure behavior-preserving changes.
5. **R8 / lint / tests / release** — [05](05-r8-lint-testing-release.md): new keep rules, shrinkResources, lint baseline,
   tests, AAB, version bump, privacy policy.
6. *(Optional, separate project)* Kotlin, ViewModel, Room, ViewPager2, Compose — also in 04.

Tooling help available locally: Android Studio **AI-262** is installed → use **Tools ▸ AGP Upgrade Assistant** for
step 1 and **Refactor ▸ Migrate to AndroidX** for step 2; both are more reliable than hand edits. The
`agp-9-upgrade` agent skill covers the AGP 9 specifics (new DSL, built-in Kotlin, kapt/KSP) — this project uses
**neither Kotlin nor kapt/KSP** (Java + `annotationProcessor`), so those parts of AGP 9 migration are N/A.

## Effort / risk summary

| Step | Effort | Risk | Why |
|---|---|---|---|
| 0 Stabilize | S | low | 3 small fixes + data decision |
| 1 Toolchain | S–M | low | tiny build script; main trap is JDK/SDK env |
| 2 AndroidX | M | **medium** | mass package/XML rename, theme enforcement of Material components, Dagger processor bump |
| 3 SDK behavior | M | **medium** | edge-to-edge + large-screen enforcement change visual layout; needs device testing |
| 4 Deprecations | M | low–medium | `AsyncTask`/menu/fragment lifecycle rewrites touch the importer and screens |
| 5 R8/lint/release | S | low | enabling obfuscation: check mapping upload, no reflection in app code |

## Definition of done for the upgrade

- `./gradlew help assembleDebug testDebugUnitTest lintDebug bundleRelease` green on JDK 21.
- App installs and works on API 23 and API 36 emulators: first-launch import, flip, swipe, add, edit, delete+undo,
  language switch, about/help pages, rotation.
- No `android.support.*` imports remain; `grep -r "android.support" src` is empty.
- Release AAB signed with the existing `dmax.keystore` (same upload key), versionCode bumped.
