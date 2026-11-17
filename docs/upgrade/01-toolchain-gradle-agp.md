# 01 — Toolchain: JDK, Gradle, AGP, `build.gradle`

## Findings

1. **JDK.** `java` on PATH and Android Studio's bundled JBR are both **JDK 25.0.4** here; `~/.gradle/gradle.properties`
   sets `org.gradle.java.home` to the Studio JBR. Gradle 8.10.2 supports up to JDK 23, hence the build failure
   `Unsupported class file major version 69`. AGP 9.5 requires **JDK ≥ 17**.
2. **Gradle.** Wrapper is 8.10.2. AGP 9.5.x requires **Gradle ≥ 9.6.0**. `~/.gradle/wrapper/dists` already contains
   `gradle-9.6.1-bin` and `gradle-9.1.0-bin`, so no extra download for 9.6.1.
3. **AGP.** `8.8.0-beta01` (pre-release) in `buildscript`. Target **9.5.1**.
4. **Build script style.** Legacy `buildscript { classpath }` + `apply plugin`, no `settings.gradle`, space-separated
   Groovy assignments (`compileSdkVersion 34`) and `lintOptions` — all deprecated; several are *removed* by AGP 9's
   new DSL (`compileSdkVersion`, `minSdkVersion`, `targetSdkVersion`, `lintOptions`).
5. **No `gradle.properties` in the repo.** The global `~/.gradle/gradle.properties` sets
   `org.gradle.daemon/configureondemand/caching/parallel/workers.max` and `kapt.*` (kapt is unused here;
   `configureondemand` is discouraged/incubating — remove it).
6. **SDK.** `~/Library/Android/sdk` has platforms `android-36`, `android-37.0`; build-tools `36.0.0`, `37.0.0`;
   `cmdline-tools/latest`. Platform 34 is missing → current `compileSdkVersion 34` can't resolve. Install via
   `sdkmanager` only if you must keep 34; the plan moves to 36 anyway.
7. **Signing.** See below — password literal regression.
8. **`local.properties`** contains `sdk.dir` and is git-ignored ✓.
9. **Default AGP 9 flips** that matter here: `android.useAndroidX` → `true` (support libs break, see 02);
   `android.enableAppCompileTimeRClass` → `true` (non-final R in app; already handled by the `switch`→`if` rewrite);
   `android.sdk.defaultTargetSdkToCompileSdkIfUnset` → `true`; `android.proguard.failOnMissingFiles` → `true`;
   `getDefaultProguardFile('proguard-android.txt')` no longer supported (project uses its own file, OK).
   Built-in Kotlin / kapt changes: **N/A** (no Kotlin).

## Steps

### 1. JDK
Install JDK 21 (e.g. Temurin or Studio's JBR if it's 21 on your Studio version) and point Gradle at it:
`org.gradle.java.home=<jdk21>` in `~/.gradle/gradle.properties` **or** set `JAVA_HOME` per shell **or** (best) let the
project declare it so every agent/CI gets the same one: add to `gradle.properties`
`org.gradle.java.installations.auto-download=true` and a toolchain resolver plugin in `settings.gradle`
(foojay `org.gradle.toolchains.foojay-resolver-convention`). Simplest for a solo project: just set `JAVA_HOME`.
If you want to keep JDK 25 as the daemon JVM, test it explicitly on Gradle 9.6.1 + AGP 9.5.1; fall back to 21 on any error.

### 2. Gradle wrapper
```bash
JAVA_HOME=<jdk21> ./gradlew wrapper --gradle-version 9.6.1 --distribution-type bin
```
Commit `gradle/wrapper/*`, `gradlew`, `gradlew.bat` (currently untracked/modified). Keep `validateDistributionUrl=true`.
Add `distributionSha256Sum` from gradle.org for supply-chain integrity (optional).

### 3. `settings.gradle` (new)
```groovy
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "plua"
```
(Optional but recommended: move sources into an `app/` module so the root can hold shared config. Not required;
if you do, `git mv src app/src` and put `build.gradle` in `app/`.)

### 4. `gradle.properties` (new, repo-level)
```properties
org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
# AGP 9 defaults already: useAndroidX=true, non-transitive R, non-final R. Don't set android.enableJetifier.
```

### 5. `build.gradle` — proposed end state (Groovy DSL, AGP 9.5.1)
```groovy
plugins {
    id 'com.android.application' version '9.5.1'
}

android {
    namespace = 'dmax.plua'
    compileSdk = 36

    defaultConfig {
        applicationId = 'dmax.plua'
        minSdk = 23            // product decision, see 03
        targetSdk = 36
        versionCode = 8
        versionName = '0.7'
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    signingConfigs {
        release {
            def pwd = providers.gradleProperty('keystorePassword').orNull   // ~/.gradle/gradle.properties
            if (pwd != null) {
                storeFile = file(System.getProperty('user.home') + '/.android/dmax.keystore')
                storePassword = pwd
                keyAlias = 'dmax'
                keyPassword = pwd
            }
        }
    }

    buildTypes {
        release {
            if (signingConfigs.release.storeFile != null) signingConfig = signingConfigs.release
            minifyEnabled = true
            shrinkResources = true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = true
        htmlReport = true
        xmlReport = false
    }
}

dependencies { /* see 02 */ }
```
Notes:
- Version name/code: the old `ext { VERSION_NAME … }` block can go; if you want one source of truth, keep it as
  plain `def`s or in `gradle.properties`.
- The `minifyEnabled`/`shrinkResources` Groovy names map to `isMinifyEnabled`/`isShrinkResources` in the new DSL.
  AGP 9.x also introduces an `optimization { … }` DSL block (see the AGP 9.0 release notes: "new optimization DSL");
  if Sync warns about the legacy flags, move to it — don't guess, read the notes of the exact AGP version.
- If any property fails under the new DSL, the Studio **AGP Upgrade Assistant** will list it; consult the
  `agp-9-upgrade` skill's `references/recipes.md` rather than web examples.
- `buildToolsVersion` should stay unset (AGP default = 36.0.0).
- Remove the manifest `package="dmax.plua"` attribute (namespace is authoritative).

### 6. Signing
Current uncommitted `build.gradle` has `storePassword "keystorePassword"` (literal) — **regression**; the property
`keystorePassword` lives in `~/.gradle/gradle.properties`. The snippet above restores the property lookup and
degrades gracefully when it's absent (debug/CI builds still work). Other options for CI: env vars
(`providers.environmentVariable`). Do not commit `.keystore` files or passwords. Consider moving to Play App Signing
with a separate upload key.

### 7. Build & verify
```bash
export JAVA_HOME=<jdk21>
./gradlew help
./gradlew build --dry-run
./gradlew assembleDebug testDebugUnitTest lintDebug
./gradlew bundleRelease          # needs keystorePassword property
./gradlew --stop                 # after switching JDKs, stop old daemons
```
Do not run `clean` as part of verification (wasteful); delete `.gradle/8.9`, `.gradle/8.10.2` caches if stale.

## Compatibility reference (AGP 9.5)

| | Min | Default |
|---|---|---|
| Gradle | 9.6.0 | 9.6.0 |
| SDK Build Tools | 36.0.0 | 36.0.0 |
| JDK | 17 | 17 |
| Max API level | 37 | |

Previous lines for fallbacks: AGP 9.0.x needs Gradle 9.1.0, build tools 36.0.0, JDK 17, max API 36.1.
If AGP 9.5.1 misbehaves, 9.2.x/9.3.x are valid intermediate targets (check their release notes for Gradle minimums).

## Pitfalls specific to this repo

- Mixing `android.support.*` artifacts with `android.useAndroidX=true` (AGP 9 default) fails the build; AGP 9 does
  not make Jetifier a good long-term answer. Migrate source (02).
- `proguardFile 'proguard-android.txt'` points at the project-local file; `proguard-android.txt` from the SDK is no
  longer a valid `getDefaultProguardFile` target in AGP 9, so renaming the local file to `proguard-rules.pro`
  avoids confusion.
- `android.proguard.failOnMissingFiles=true` by default: a typo'd keep-file name now fails the build.
- If `lint` complains about `lintOptions`, you missed one of the legacy blocks.
